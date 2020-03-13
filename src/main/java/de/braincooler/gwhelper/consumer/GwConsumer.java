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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GwConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwConsumer.class);
    private static List<Integer> enemySynd = Arrays.asList(8866, 7007, 167, 5285, 75, 4549, 7836, 7070, 7627, 9596,
            3696, 2507, 7543, 1653, 8133, 2083, 15, 6366, 6008, 3302, 7711, 9393, 116, 2589, 1079, 9884, 1414, 1608,
            7351, 7119, 9563, 5352, 3667, 7161, 309, 1539, 6001, 103, 6776, 2150, 1752, 5300, 1378, 9469);

    private WebClient webClient;
    private CredService credService;

    private List<Building> buildings = new ArrayList<>();
    private Set<String> notReadablePages = new HashSet<>();

    public GwConsumer(CredService credService) {
        this.credService = credService;
        initWebClient();
    }

    public List<Building> getSektorBuilings() {
        initSektorObjects();
        return new ArrayList<>(buildings);
    }

    public void initSektorObjects() {
        for (int i = 47; i <= 53; i++) {
            for (int j = 47; j <= 53; j++) {
                LOGGER.info("---------------------------------------------------------------");
                LOGGER.info("init x={}, y={}", i, j);
                LOGGER.info("plants..");
                initBuildingsFromSektorPage(i, j, "plants");
                LOGGER.info("tech..");
                initBuildingsFromSektorPage(i, j, "tech");
                LOGGER.info("---------------------------------------------------------------");
            }
        }
    }

    private void initBuildingsFromSektorPage(int sektorX, int sektorY, String type) {
        String url = String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=%s", sektorX, sektorY, type);

        HtmlPage htmlPage;
        try {
            LOGGER.info("init buildings from url={}", url);
            Thread.sleep(500);
            htmlPage = webClient.getPage(url);
            HtmlTable table = (HtmlTable) htmlPage.getByXPath("//*[@id=\"mapcontents\"]/table[1]/tbody/tr/td/table[1]").get(0);

            List<HtmlTableRow> tableRows = table.getRows();
            for (int i = 2; i < tableRows.size(); i++) {
                HtmlTableRow row = tableRows.get(i);
                List<HtmlTableCell> cells = row.getCells();
                HtmlTableCell firstCell = cells.get(0);
                String classAttr = firstCell.getAttribute("class");
                if (!classAttr.equals("greenbg") && !classAttr.equals("greengreenbg")) {

                    String controlSyndRef = "syndicate.php?id=0";
                    String areaRef = "";
                    String objectRef = firstCell
                            .getChildNodes()
                            .get(0)
                            .getAttributes()
                            .getNamedItem("href")
                            .getNodeValue();
                    if (objectRef.contains("syndicate.php?id")) {
                        controlSyndRef = objectRef;
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
                        ownerSyndId = Integer.parseInt(ownerSyndRef.substring(ownerSyndRef.indexOf("=") + 1));
                    }
                    int controlSyndId = Integer.parseInt(controlSyndRef.substring(controlSyndRef.indexOf("=") + 1));
                    int area = 0;
                    if (areaRef.contains("(")) {
                        area = Integer.parseInt(areaRef.substring(areaRef.indexOf("(") + 1, areaRef.indexOf(")")));
                    }
                    if (enemySynd.contains(controlSyndId)) {
                        Building building = new Building();
                        building.setRef("http://www.gwars.ru" + objectRef);
                        building.setOwnerSynd(ownerSyndId);
                        building.setControlSynd(controlSyndId);
                        building.setSektorUrl(url);
                        building.setArea(area);
                        buildings.add(building);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- <<< --- page not readable url={} --->>> --- >>>", url);
            notReadablePages.add(url);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("getSektorObject(): error loading page url={}", url);
        }
    }

    public int getBuildingOwnerSyndicateId(String url) {
        try {
            HtmlPage site = webClient.getPage(url);
            HtmlTable table = (HtmlTable) site.getByXPath("//table").get(4);
            String value = table.getRow(0).asText();
            value = value.substring(value.indexOf("#") + 1, value.length() - 1);
            return Integer.parseInt(value);
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- age not readable url={} --->>>", url);
            notReadablePages.add(url);
        } catch (IOException ex) {
            LOGGER.error("error loading object info");
        }
        return 0;
    }

    // 1. full link 2. controlled syndicate
    public Map<String, Integer> getMapTargetBuildingAndSyndId() {
        String url = "http://www.gwars.ru/syndicate.php?id=1635&page=targets";
        final HtmlPage page;
        Map<String, Integer> result = new HashMap<>();
        try {
            page = webClient.getPage(url);
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
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- page not readable url={} --->>>", url);
            notReadablePages.add(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Set<String> getNotReadablePages() {
        return notReadablePages;
    }

    public LocalDateTime getAtackTime(int buildingId) {
        String url = "http://www.gwars.ru/objectworkers.php?id=" + buildingId;
        try {
            final HtmlPage page = webClient.getPage(url);
            List<HtmlNoBreak> byXPath = page.getByXPath("//nobr");
            List<HtmlNoBreak> timeNoBrs = byXPath.stream()
                    .filter(htmlNoBreak -> htmlNoBreak.asText().contains("Следующее нападение возможно после"))
                    .collect(Collectors.toList());
            if (timeNoBrs.size() > 0) {
                String atackTimeString = timeNoBrs.get(0).asText();
                atackTimeString = atackTimeString.substring(atackTimeString.indexOf("после ") + 6, atackTimeString.length() - 1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
                return LocalDateTime.parse(atackTimeString, formatter);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- page not readable url={} --->>>", url);
            notReadablePages.add(url);
        } catch (IOException ex) {
            LOGGER.error("error getting attack time", ex);
        }

        return LocalDateTime.MIN;
    }

    public int getStaticControlSyndId(int buildingId) {
        String url = "http://www.gwars.ru/object.php?id=" + buildingId;
        LOGGER.info("getStaticControlSyndId() " + url);
        int staticControlSyndId = 0;
        try {
            Thread.sleep(500);
            final HtmlPage page = webClient.getPage(url);
            HtmlTable table = (HtmlTable) page.getByXPath("/html/body/div[3]/table[2]/tbody/tr/td/table[1]").get(0);
            String value = table.getRow(0).asText();
            LOGGER.info(" -> value={}", value);
            if (value.contains("#")) {
                staticControlSyndId = Integer.parseInt(value.substring(value.lastIndexOf("#") + 1, value.length() - 1));
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- page not readable url={} --->>>", url);
            notReadablePages.add(url);
        } catch (NumberFormatException ex) {
            LOGGER.error("error parsing string: {}", ex.getLocalizedMessage());
        } catch (IOException | InterruptedException e) {
            LOGGER.error("error reading url={}", url);
        }
        return staticControlSyndId;
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
