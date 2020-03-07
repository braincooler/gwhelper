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
import java.util.List;

@Service
public class GwConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwConsumer.class);

    private WebClient webClient;
    private CredService credService;

    public GwConsumer(CredService credService) {
        this.credService = credService;
        initWebClient();
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
        this.webClient = new WebClient(BrowserVersion.CHROME);
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
