package de.braincooler.gwhelper;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import de.braincooler.gwhelper.config.CredService;
import de.braincooler.gwhelper.model.Sektor;
import de.braincooler.gwhelper.model.SektorHtmlTablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class GwWebClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwWebClient.class);

    private final CredService credService;

    @Value(value = "${gw.timeout.ms}")
    private long timeout;

    private WebClient webClient;

    public GwWebClient(CredService credService) {
        this.credService = credService;
    }

    public SektorHtmlTablePair fetchOnSalePageFromSektor(int x, int y) {
        String url = String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=onsale", x, y);
        HtmlPage htmlPage = getPage(url);
        HtmlTable table = (HtmlTable) Objects.requireNonNull(htmlPage)
                .getByXPath("//*[@id=\"mapcontents\"]/table[1]/tbody/tr/td/table[1]").get(0);

        Sektor sektor = buildSektor(htmlPage, x, y);
        return new SektorHtmlTablePair(sektor, table.getRows());
    }

    public HtmlPage fetchSyndWarsPage(int syndId) {
        String url = String.format("http://www.gwars.ru/syndicate.php?id=%d&page=politics", syndId);
        return getPage(url);
    }

    public HtmlTableRow fetchControledSektorsTableRow(int syndId) {
        String url = String.format("http://www.gwars.ru/syndicate.php?id=%d", syndId);
        return (HtmlTableRow) Objects.requireNonNull(getPage(url))
                .getByXPath("/html/body/div[3]/table[2]/tbody/tr[3]").get(0);
    }

    public List<HtmlTableRow> fetchBuildingTable(int sektorX, int sektorY, String type) {
        String sektorUrl = String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=%s", sektorX, sektorY, type);

        HtmlPage htmlPage = getPage(sektorUrl);
        HtmlTable table = (HtmlTable) Objects.requireNonNull(htmlPage)
                .getByXPath("//*[@id=\"mapcontents\"]/table[1]/tbody/tr/td/table[1]").get(0);

        return table.getRows();
    }

    public HtmlPage fetchBuildingLogPage(int buildingId) {
        String url = "http://www.gwars.ru/objectworkers.php?id=" + buildingId;
        return getPage(url);
    }

    public int fetchBuildingOwnerSyndicateId(String url) {
        HtmlPage site = getPage(url);
        assert site != null;
        HtmlTable table = (HtmlTable) site.getByXPath("//table").get(4);
        String value = table.getRow(0).asText();
        value = value.substring(value.indexOf("#") + 1, value.length() - 1);
        return Integer.parseInt(value);
    }

    public HtmlPage fetchBuildingInfoPage(int buildingId) {
        String url = "http://www.gwars.ru/object.php?id=" + buildingId;
        return getPage(url);
    }

    private HtmlPage getPage(String url) {
        try {
            Thread.sleep(timeout);
            return webClient.getPage(url);
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("<<< --- page not readable url={} --->>>", url);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error("error loading object info, url={}", url);
        }
        return null;
    }

    @PostConstruct
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

    private Sektor buildSektor(HtmlPage htmlPage, int x, int y) {
        HtmlSelect htmlSelect = Objects.requireNonNull(htmlPage)
                .getFirstByXPath("//*[@id=\"mapcontents\"]/center/table/tbody/tr/td[1]/select");
        String selectedOption = htmlSelect.getOption(htmlSelect.getSelectedIndex()).asText();
        String island = selectedOption.substring(1, 2);
        String sektorName = selectedOption.substring(4);

        return new Sektor(island, sektorName, x, y);
    }
}
