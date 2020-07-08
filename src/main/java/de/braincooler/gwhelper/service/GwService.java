package de.braincooler.gwhelper.service;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import de.braincooler.gwhelper.GwWebClient;
import de.braincooler.gwhelper.model.Advertisement;
import de.braincooler.gwhelper.model.Amount;
import de.braincooler.gwhelper.model.Building;
import de.braincooler.gwhelper.model.SektorHtmlTablePair;
import de.braincooler.gwhelper.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GwService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwService.class);

    private final GwWebClient gwWebClient;
    private final AdvertisementRepository advertisementRepository;

    public GwService(GwWebClient gwWebClient, AdvertisementRepository advertisementRepository) {
        this.gwWebClient = gwWebClient;
        this.advertisementRepository = advertisementRepository;
    }

    public void initSektorOnSale(int x, int y) {
        SektorHtmlTablePair sektorHtmlTablePair = gwWebClient.fetchOnSalePageFromSektor(x, y);
        LOGGER.info("<--- {} [{}:{}] --->", sektorHtmlTablePair.getSektor().getName(),
                sektorHtmlTablePair.getSektor().getX(), sektorHtmlTablePair.getSektor().getY());
        advertisementRepository.removeOld(sektorHtmlTablePair.getSektor());
        for (int i = 1; i < sektorHtmlTablePair.getHtmlTableRows().size(); i++) {
            HtmlTableRow row = sektorHtmlTablePair.getHtmlTableRows().get(i);
            List<HtmlTableCell> cells = row.getCells();
            HtmlTableCell cell1 = cells.get(0);
            String buildingRef = cell1
                    .getChildNodes()
                    .get(0)
                    .getAttributes()
                    .getNamedItem("href")
                    .getNodeValue();
            int buildingId = Integer.parseInt(
                    buildingRef.substring(buildingRef.indexOf("=") + 1));

            String advertisementText = cell1
                    .getChildNodes()
                    .get(1)
                    .asText();
            int buildingArea = Integer.parseInt(
                    advertisementText.substring(advertisementText.indexOf("(") + 1, advertisementText.indexOf(")"))
            );
            Amount buildingAmount = getBuildingPrice(advertisementText);
            String buildingType = cell1
                    .getChildNodes()
                    .get(0).asText();

            HtmlTableCell cell2 = cells.get(1);
            DomNodeList<DomNode> ownerAndSyndNodes = cell2
                    .getChildNodes()
                    .get(0)
                    .getChildNodes()
                    .get(0)
                    .getChildNodes();
            String ownerRef;
            if (ownerAndSyndNodes.get(1) == null) {
                ownerRef = ownerAndSyndNodes.get(0)
                        .getAttributes()
                        .getNamedItem("href")
                        .getNodeValue();
            } else {
                ownerRef = ownerAndSyndNodes.get(1)
                        .getAttributes()
                        .getNamedItem("href")
                        .getNodeValue();
            }

            String ownerName = cell2.getChildNodes().get(0).asText();

            int ownerId = Integer.parseInt(ownerRef.substring(ownerRef.indexOf("=") + 1));

            Building building = new Building(buildingId, buildingType, buildingArea, ownerId, ownerName);
            Advertisement advertisement = new Advertisement(sektorHtmlTablePair.getSektor(), building, buildingAmount);

            //LOGGER.info(i + ". id=" + buildingId + " type=" + buildingType +
            //       " area=" + buildingArea + " price=" + buildingAmount.getPrice() + " " + buildingAmount.getCurrency() + " ownerRef: " + ownerRef + ".");
            //
            advertisementRepository.addAdvertisement(advertisement);
        }
    }

    private Amount getBuildingPrice(String advertisementText) {
        int price;
        if (advertisementText.endsWith("Гб")) {
            price = Integer.parseInt(
                    advertisementText.substring(advertisementText.indexOf(")") + 5, advertisementText.length() - 3)
                            .replace(",", ""));
            return new Amount(price, "Gb");
        } else {
            price = Integer.parseInt(
                    advertisementText.substring(advertisementText.indexOf(")") + 5, advertisementText.length() - 4)
                            .replace(",", ""));
            return new Amount(price, "EUN");
        }

    }

    public Map<Integer, Advertisement> getAllAdvertisements() {
        return advertisementRepository.getAll();
    }
}
