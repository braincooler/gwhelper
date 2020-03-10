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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GwConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwConsumer.class);

    private WebClient webClient;
    private CredService credService;

    private Map<String, String> sektorObjectsToOwnerSyndId = new HashMap<>();
    private Map<String, String> logs = new HashMap<>();

    public GwConsumer(CredService credService) {
        this.credService = credService;
        initWebClient();
    }

    public Map<String, String> getSektorObjects() {
        return new HashMap<>(sektorObjectsToOwnerSyndId);
    }

    @Scheduled(fixedDelay = 600000)
    public void initSektorObjects() {
        try {
            for (int i = 47; i <= 53; i++) {
                for (int j = 47; j <= 53; j++) {
                    String url = String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=", i, j);
                    logs.put("URL: " + url + "plants", "");
                    fillObjectsMapFromSektorPage(webClient.getPage(url + "plants"));
                    Thread.sleep(500);
                    logs.put("URL: " + url + "tech", "");
                    fillObjectsMapFromSektorPage(webClient.getPage(url + "tech"));
                }
            }


        } catch (IOException | InterruptedException ex) {
            LOGGER.error("getSektorObject(): error loading page");
        }
    }

    private void fillObjectsMapFromSektorPage(HtmlPage htmlPage) {
        HtmlTable table = (HtmlTable) htmlPage.getByXPath("//*[@id=\"mapcontents\"]/table[1]/tbody/tr/td/table[1]").get(0);

        List<HtmlTableRow> tableRows = table.getRows();
        for (int i = 2; i < tableRows.size(); i++) {
            HtmlTableRow row = tableRows.get(i);
            List<HtmlTableCell> cells = row.getCells();
            HtmlTableCell firstCell = cells.get(0);
            String classAttr = firstCell.getAttribute("class");
            if (!classAttr.equals("greenbg") && !classAttr.equals("greengreenbg")) {
                String objectRef = firstCell
                        .getChildNodes()
                        .get(1)
                        .getAttributes()
                        .getNamedItem("href")
                        .getNodeValue();

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
                String ownerSyndId = "0";
                if (ownerSyndRef.contains("syndicate.php?id")) {
                    ownerSyndId = ownerSyndRef.substring(ownerSyndRef.indexOf("=") + 1);
                }
                sektorObjectsToOwnerSyndId.put(objectRef, ownerSyndId);
            }
        }
    }

    public int getBuildingOwnerSyndicateId(String url) {
        try {
            HtmlPage site = webClient.getPage(url);
            HtmlTable table = (HtmlTable) site.getByXPath("//table").get(4);
            String value = table.getRow(0).asText();
            value = value.substring(value.indexOf("#") + 1, value.length() - 1);
            return Integer.parseInt(value);
        } catch (IOException ex) {
            LOGGER.error("error loading object info");
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("getOwnerSyndicateId(): error reading url: {}", url);
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("get1635TargetStrings(): error reading url: {}", url);
        }

        return result;
    }

    public double getRibaMinPrice() {
        try {
            HtmlTable table = getRibaHtmlTable();
            List<HtmlTableRow> rows = table.getRows();
            HtmlTableRow row = rows.get(3);
            List<HtmlTableCell> cells = row.getCells();
            HtmlTableCell cell = cells.get(0);
            double price = fetchPrice(cell.asText());
            HtmlTableCell cell2 = cells.get(1);
            double count = Integer.parseInt(cell2.asText());
            return price / count;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void fetchRibaPrice() {
        try {
            HtmlTable table = getRibaHtmlTable();
            List<HtmlTableRow> rows = table.getRows();
            for (int i = 3; i < rows.size(); i++) {
                HtmlTableRow row = rows.get(i);
                List<HtmlTableCell> cells = row.getCells();
                HtmlTableCell cell = cells.get(0);
                double price = fetchPrice(cell.asText());
                HtmlTableCell cell2 = cells.get(1);
                double count = Integer.parseInt(cell2.asText());

            }
        } catch (IOException ex) {
            LOGGER.error("error fetching HtmlTable", ex);
        }

    }

    private int fetchPrice(String s) {
        s = s.substring(0, s.indexOf("$")).replace(",", "");
        return Integer.parseInt(s);
    }

    private HtmlTable getRibaHtmlTable() throws IOException {
        final HtmlPage riba = webClient.getPage("https://www.gwars.ru/market.php?stage=2&item_id=perch&action_id=1&island=0");
        return (HtmlTable) riba.getByXPath("//table[@class='withborders']").get(0);

    }

    private void initWebClient() {
        this.webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
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

    public Map<String, String> getLogs() {
        return logs;
    }
}
