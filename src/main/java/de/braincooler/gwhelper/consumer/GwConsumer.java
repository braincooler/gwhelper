package de.braincooler.gwhelper.consumer;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import de.braincooler.gwhelper.config.CredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GwConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwConsumer.class);
    private static List<Integer> enemySynd = Arrays.asList(8012, 7639, 8866, 7007, 167, 5285, 75, 4549, 7836, 7070, 7627, 9596,
            3696, 2507, 7543, 1653, 8133, 2083, 15, 6366, 6008, 3302, 7711, 9393, 116, 2589, 1079, 9884, 1414, 1608,
            7351, 7119, 9563, 5352, 3667, 7161, 309, 1539, 6001, 103, 6776, 2150, 1752, 5300, 1378, 9469);

    @Value(value = "${gw.timeout.ms}")
    private long timeout;

    private WebClient webClient;
    private final CredService credService;
    private final BuildingRepository buildingRepository;

    private Set<String> notReadablePages = new HashSet<>();
    private int counter;

    public GwConsumer(CredService credService, BuildingRepository buildingRepository) {
        this.credService = credService;
        this.buildingRepository = buildingRepository;
        initWebClient();
    }

    private HtmlPage getPage(String url) {
        try {
            Thread.sleep(timeout);
            return webClient.getPage(url);
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- page not readable url={} --->>>", url);
            notReadablePages.add(url);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("error loading object info, url={}", url);
        }
        return null;
    }

    public void initSektorObjects() {
        LocalDateTime timerStart = LocalDateTime.now();
        LOGGER.info("<<< --- start --- >>>");
        counter = 0;
        for (int i = 47; i <= 53; i++) {
            for (int j = 47; j <= 53; j++) {
                initBuildingsFromSektorPage(i, j, "plants");
                initBuildingsFromSektorPage(i, j, "tech");
            }
        }
        notReadablePages.add(String.format("buildings: %d", counter));
        LOGGER.info("<<< --- end [{} - {}}] --- >>>", timerStart, LocalDateTime.now());
    }

    private void initBuildingsFromSektorPage(int sektorX, int sektorY, String type) {
        String sektorUrl = String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=%s", sektorX, sektorY, type);

        HtmlPage htmlPage = getPage(sektorUrl);
        HtmlTable table = (HtmlTable) htmlPage.getByXPath("//*[@id=\"mapcontents\"]/table[1]/tbody/tr/td/table[1]").get(0);

        List<HtmlTableRow> tableRows = table.getRows();
        for (int i = 2; i < tableRows.size(); i++) {
            HtmlTableRow row = tableRows.get(i);
            List<HtmlTableCell> cells = row.getCells();
            HtmlTableCell firstCell = cells.get(0);
            String classAttr = firstCell.getAttribute("class");
            if (!classAttr.equals("greenbg") && !classAttr.equals("greengreenbg")) {
                counter++;
                String currentControlSyndRef = "syndicate.php?id=0";
                String areaRef = "";
                String objectRef = firstCell
                        .getChildNodes()
                        .get(0)
                        .getAttributes()
                        .getNamedItem("href")
                        .getNodeValue();
                if (objectRef.contains("syndicate.php?id")) {
                    currentControlSyndRef = objectRef;
                    objectRef = firstCell
                            .getChildNodes()
                            .get(1)
                            .getAttributes()
                            .getNamedItem("href")
                            .getNodeValue();
                    areaRef = firstCell
                            .getChildNodes()
                            .get(2)
                            .asText();
                }

                String ownerSyndRef = cells.get(1)
                        .getChildNodes()
                        .get(0)
                        .getChildNodes()
                        .get(0)
                        .getChildNodes()
                        .get(0)
                        .getAttributes()
                        .getNamedItem("href")
                        .getNodeValue();

                int ownerSyndId = 0;
                if (ownerSyndRef.contains("syndicate.php?id")) {
                    try {
                        ownerSyndId = Integer.parseInt(ownerSyndRef.substring(ownerSyndRef.indexOf("=") + 1));
                    } catch (NumberFormatException ex) {
                        LOGGER.error("error parsing '{}'", ownerSyndRef);
                    }
                }
                int currentControlSyndId = 0;
                try {
                    currentControlSyndId = Integer.parseInt(
                            currentControlSyndRef.substring(currentControlSyndRef.indexOf("=") + 1));
                } catch (NumberFormatException ex) {
                    LOGGER.error("error parsing '{}'", currentControlSyndRef);
                }
                int area = 0;
                if (areaRef.contains("(")) {
                    try {
                        area = Integer.parseInt(
                                areaRef.substring(areaRef.indexOf("(") + 1, areaRef.indexOf(")")));
                    } catch (NumberFormatException ex) {
                        LOGGER.error("error parsing '{}'", areaRef);
                    }
                }

                Building building = new Building();
                building.setRef("http://www.gwars.ru" + objectRef);
                if (enemySynd.contains(currentControlSyndId) || ownerSyndId == 1635) {
                    HtmlPage buildingLogPage = fetchBuildingLogPage(building.getId());
                    LocalDateTime readyForAtackTime = readAtackTime(buildingLogPage);
                    if (readyForAtackTime.isBefore(LocalDateTime.now(ZoneId.of("Europe/Moscow")))) {
                        HtmlPage buildingInfoPage = fetchBuildingInfoPage(building.getId());
                        String buildingInfo = readBuildingInfo(buildingInfoPage);
                        String sektorName = readSektorName(buildingInfoPage);
                        building.setSektorName(sektorName);
                        building.setOwnerSynd(ownerSyndId);
                        building.setControlSynd(currentControlSyndId);
                        building.setSektorUrl(sektorUrl);
                        building.setArea(area);

                        int staticControlSyndId = 0;
                        if (buildingInfo.contains("#")) {
                            try {
                                staticControlSyndId = Integer.parseInt(
                                        buildingInfo.substring(
                                                buildingInfo.lastIndexOf("#") + 1,
                                                buildingInfo.length() - 1));
                            } catch (NumberFormatException ex) {
                                LOGGER.error("error parsing '{}'", buildingInfo);
                            }
                        }
                        building.setStaticControlsyndId(staticControlSyndId);

                        building.setDescription(buildingInfo);
                        if (building.getControlSynd() != building.getStaticControlsyndId() &&
                                !buildingInfo.contains("Сектор [G]") ||
                                building.getOwnerSynd() == 15 ||
                                (building.getOwnerSynd() == 1635 && currentControlSyndId != 1635)) {
                            buildingRepository.save(building);
                        } else {
                            buildingRepository.delete(building);
                        }
                    }
                } else {
                    buildingRepository.delete(building);
                }
            }
        }
    }

    private String readSektorName(HtmlPage buildingInfoPage) {
        List<Object> list = buildingInfoPage.getByXPath("/html/body/div[3]/table[2]/tbody/tr/td/div");
        if (list.size() > 0) {
            String sektorName = ((HtmlDivision) list.get(0)).asText();
            sektorName = sektorName.substring(sektorName.indexOf("]") + 2, sektorName.indexOf("Рабочие места"));
            return sektorName;
        }
        return "no value";
    }

    public int fetchBuildingOwnerSyndicateId(String url) {
        HtmlPage site = getPage(url);
        HtmlTable table = (HtmlTable) site.getByXPath("//table").get(4);
        String value = table.getRow(0).asText();
        value = value.substring(value.indexOf("#") + 1, value.length() - 1);
        return Integer.parseInt(value);
    }

    // 1. full link 2. controlled syndicate
    public Map<String, Integer> fetchMapTargetBuildingAndSyndId() {
        String url = "http://www.gwars.ru/syndicate.php?id=1635&page=targets";
        Map<String, Integer> result = new HashMap<>();
        HtmlPage page = getPage(url);
        HtmlTable table = (HtmlTable) page.getByXPath("//table[@class='bordersupdown']")
                .get(1);

        List<HtmlTableRow> tableRows = table.getRows();

        for (int i = 2; i < tableRows.size(); i++) {
            HtmlTableRow row = tableRows.get(i);
            List<HtmlTableCell> cells = row.getCells();
            String objectRef = cells.get(1)
                    .getFirstChild()
                    .getChildNodes()
                    .get(2)
                    .getAttributes()
                    .getNamedItem("href")
                    .getNodeValue();

            String sind = cells.get(0).getFirstChild()
                    .getChildNodes()
                    .get(1)
                    .asText();
            sind = sind.substring(sind.indexOf("#") + 1);
            Integer sindId = Integer.parseInt(sind);
            result.put("http://www.gwars.ru" + objectRef, sindId);
        }
        return result;
    }

    public HtmlPage fetchBuildingLogPage(int buildingId) {
        String url = "http://www.gwars.ru/objectworkers.php?id=" + buildingId;
        return getPage(url);
    }

    public LocalDateTime readAtackTime(HtmlPage buildingLogPage) {
        List<HtmlNoBreak> byXPath = buildingLogPage.getByXPath("//nobr");
        List<HtmlNoBreak> timeNoBrs = byXPath.stream()
                .filter(htmlNoBreak -> htmlNoBreak.asText().contains("Следующее нападение возможно после"))
                .collect(Collectors.toList());
        if (timeNoBrs.size() > 0) {
            String atackTimeString = timeNoBrs.get(0).asText();
            atackTimeString = atackTimeString.substring(atackTimeString.indexOf("после ") + 6, atackTimeString.length() - 1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
            return LocalDateTime.parse(atackTimeString, formatter);
        }
        return LocalDateTime.MIN;
    }

    public HtmlPage fetchBuildingInfoPage(int buildingId) {
        String url = "http://www.gwars.ru/object.php?id=" + buildingId;
        return getPage(url);
    }

    public String readBuildingInfo(HtmlPage buildingPage) {
        HtmlTable table = (HtmlTable) buildingPage.getByXPath("/html/body/div[3]/table[2]/tbody/tr/td/table[1]").get(0);
        String result = table.getRow(0).asText();
        return result == null || result.isEmpty() ? "no value" : result;
    }

    public Set<String> getNotReadablePages() {
        return new HashSet<>(notReadablePages);
    }

    private void initWebClient() {
        this.webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
        this.webClient.getOptions().setJavaScriptEnabled(false);
        this.webClient.getOptions().setThrowExceptionOnScriptError(false);
        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        this.webClient.getOptions().setCssEnabled(false);
        this.webClient.getOptions().setDownloadImages(false);

        webClient.setCssErrorHandler(new CSSErrorHandler() {
            @Override
            public void warning(CSSParseException e) throws CSSException {

            }

            @Override
            public void error(CSSParseException e) throws CSSException {

            }

            @Override
            public void fatalError(CSSParseException e) throws CSSException {

            }
        });

        try {
            final HtmlPage page1 = webClient.getPage("https://www.gwars.ru/login.php?");
            HtmlForm form = page1.getElementByName("myform");
            HtmlInput login = form.getInputByName("login");
            HtmlInput password = form.getInputByName("pass");
            HtmlSubmitInput button = form.getInputByValue("Войти");

            login.type(credService.getGwUser());
            password.type(credService.getGwPassword());

            button.click();
        } catch (IOException ex) {
            LOGGER.error("error initializing web client", ex);
        }
    }
}
