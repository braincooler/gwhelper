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
import de.braincooler.gwhelper.persistance.BuildingEntity;
import de.braincooler.gwhelper.persistance.PriceHistoryEntity;
import de.braincooler.gwhelper.repository.AdvertisementRepository;
import de.braincooler.gwhelper.repository.BuildingRepository;
import de.braincooler.gwhelper.repository.PriceHistoryRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AdvertisementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvertisementService.class);

    private final GwWebClient gwWebClient;
    private final AdvertisementRepository advertisementRepository;
    private final BuildingRepository buildingRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public AdvertisementService(GwWebClient gwWebClient, AdvertisementRepository advertisementRepository, BuildingRepository buildingRepository, PriceHistoryRepository priceHistoryRepository) {
        this.gwWebClient = gwWebClient;
        this.advertisementRepository = advertisementRepository;
        this.buildingRepository = buildingRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Transactional
    public void initSektorOnSale(int x, int y) {
        SektorHtmlTablePair sektorHtmlTablePair = gwWebClient.fetchOnSalePageFromSektor(x, y);
        LOGGER.info("<--- {} [{}:{}] --->", sektorHtmlTablePair.getSektor().getName(),
                sektorHtmlTablePair.getSektor().getX(), sektorHtmlTablePair.getSektor().getY());
        List<BuildingEntity> persistedBuilding = buildingRepository.
                findAllBySektorName(sektorHtmlTablePair.getSektor().getName());
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
            advertisementRepository.addAdvertisement(advertisement);

            BuildingEntity buildingEntity;
            Optional<BuildingEntity> buildingEntityOpt = persistedBuilding.stream()
                    .filter(entity -> entity.getGwId() == buildingId)
                    .findFirst();

            if (buildingEntityOpt.isPresent()) {
                buildingEntity = buildingEntityOpt.get();
                buildingEntity.setOnSale(true);

                List<PriceHistoryEntity> priceHistory = buildingEntity.getPriceHistory();
                Hibernate.initialize(priceHistory);
                PriceHistoryEntity lastHistory = priceHistory.stream()
                        .max(Comparator.comparing(PriceHistoryEntity::getTimestamp))
                        .orElseThrow(NoSuchElementException::new);
                if (lastHistory.getPrice() != advertisement.getAmount().getPrice()) {
                    PriceHistoryEntity priceHistoryEntity = new PriceHistoryEntity();
                    priceHistoryEntity.setTimestamp(Instant.now().getEpochSecond());
                    priceHistoryEntity.setPrice(buildingAmount.getPrice());
                    priceHistoryEntity.setCurrency(buildingAmount.getCurrency());

                    buildingEntity.addHistory(priceHistoryEntity);
                }
                persistedBuilding.remove(buildingEntity);
            } else {
                buildingEntity = new BuildingEntity();
                buildingEntity.setGwId(buildingId);
                buildingEntity.setOnSale(true);
                buildingEntity.setOwnerId(ownerId);
                buildingEntity.setSektorName(advertisement.getSektor().getName());

                PriceHistoryEntity priceHistoryEntity = new PriceHistoryEntity();
                priceHistoryEntity.setTimestamp(Instant.now().getEpochSecond());
                priceHistoryEntity.setPrice(buildingAmount.getPrice());
                priceHistoryEntity.setCurrency(buildingAmount.getCurrency());

                buildingEntity.addHistory(priceHistoryEntity);
            }

            buildingRepository.save(buildingEntity);
        }

        if (!persistedBuilding.isEmpty()) {
            for (BuildingEntity entity : persistedBuilding) {
                entity.setOnSale(false);

                PriceHistoryEntity priceHistoryEntity = new PriceHistoryEntity();
                priceHistoryEntity.setTimestamp(Instant.now().getEpochSecond());
                priceHistoryEntity.setPrice(0);
                priceHistoryEntity.setCurrency("Eun");

                entity.addHistory(priceHistoryEntity);

                buildingRepository.save(entity);
            }
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
            return new Amount(price, "Eun");
        }
    }
}
