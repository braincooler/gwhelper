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
import de.braincooler.gwhelper.persistance.AdvertisementEntity;
import de.braincooler.gwhelper.persistance.PriceHistoryEntity;
import de.braincooler.gwhelper.repository.AdvertisementDBRepository;
import de.braincooler.gwhelper.repository.LocalRepository;
import de.braincooler.gwhelper.repository.PriceHistoryRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final GwWebClient gwWebClient;
    private final LocalRepository localRepository;
    private final AdvertisementDBRepository advertisementDBRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public AdvertisementService(GwWebClient gwWebClient,
                                LocalRepository localRepository,
                                AdvertisementDBRepository advertisementDBRepository,
                                PriceHistoryRepository priceHistoryRepository) {
        this.gwWebClient = gwWebClient;
        this.localRepository = localRepository;
        this.advertisementDBRepository = advertisementDBRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Transactional
    public void initSektorOnSale(int x, int y) {
        SektorHtmlTablePair sektorHtmlTablePair = gwWebClient.fetchOnSalePageFromSektor(x, y);
        List<AdvertisementEntity> persistedBuilding = advertisementDBRepository.
                findAllBySektorName(sektorHtmlTablePair.getSektor().getName());
        localRepository.addSektor(sektorHtmlTablePair.getSektor());

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

            AdvertisementEntity advertisementEntity;
            Optional<AdvertisementEntity> buildingEntityOpt = persistedBuilding.stream()
                    .filter(entity -> entity.getGwBuildingId() == buildingId)
                    .findFirst();

            if (buildingEntityOpt.isPresent()) {
                advertisementEntity = buildingEntityOpt.get();
                advertisementEntity.setActive(true);

                List<PriceHistoryEntity> priceHistory = advertisementEntity.getPriceHistory();
                Hibernate.initialize(priceHistory);
                PriceHistoryEntity lastHistory = priceHistory.stream()
                        .max(Comparator.comparing(PriceHistoryEntity::getTimestamp))
                        .orElseThrow(NoSuchElementException::new);
                if (lastHistory.getPrice() != advertisement.getAmount().getPrice()) {
                    PriceHistoryEntity priceHistoryEntity = new PriceHistoryEntity();
                    priceHistoryEntity.setTimestamp(Instant.now().getEpochSecond());
                    priceHistoryEntity.setPrice(buildingAmount.getPrice());
                    priceHistoryEntity.setCurrency(buildingAmount.getCurrency());

                    advertisementEntity.addHistory(priceHistoryEntity);
                }
                persistedBuilding.remove(advertisementEntity);
            } else {
                advertisementEntity = new AdvertisementEntity();
                advertisementEntity.setGwBuildingId(buildingId);
                advertisementEntity.setActive(true);
                advertisementEntity.setGwOwnerId(ownerId);
                advertisementEntity.setArea(buildingArea);
                advertisementEntity.setSektorName(advertisement.getSektor().getName());
                advertisementEntity.setOwnerName(ownerName);
                advertisementEntity.setType(buildingType);

                PriceHistoryEntity priceHistoryEntity = new PriceHistoryEntity();
                priceHistoryEntity.setTimestamp(Instant.now().getEpochSecond());
                priceHistoryEntity.setPrice(buildingAmount.getPrice());
                priceHistoryEntity.setCurrency(buildingAmount.getCurrency());

                advertisementEntity.addHistory(priceHistoryEntity);
            }

            advertisementDBRepository.save(advertisementEntity);
        }

        if (!persistedBuilding.isEmpty()) {
            for (AdvertisementEntity entity : persistedBuilding) {
                entity.setActive(false);

                PriceHistoryEntity priceHistoryEntity = new PriceHistoryEntity();
                priceHistoryEntity.setTimestamp(Instant.now().getEpochSecond());
                priceHistoryEntity.setPrice(0);
                priceHistoryEntity.setCurrency("Eun");

                entity.addHistory(priceHistoryEntity);

                advertisementDBRepository.save(entity);
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

    public List<Advertisement> getAllOnSale() {
        return advertisementDBRepository.findAllByIsActive(true).stream()
                .map(advertisementEntity -> {
                    Building building = new Building(
                            advertisementEntity.getGwBuildingId(),
                            advertisementEntity.getType(),
                            advertisementEntity.getArea(),
                            advertisementEntity.getGwOwnerId(),
                            advertisementEntity.getOwnerName()
                    );

                    List<PriceHistoryEntity> priceHistory = advertisementEntity.getPriceHistory();
                    PriceHistoryEntity lastPrice = priceHistory.get(priceHistory.size() - 1);
                    Amount amount = new Amount(lastPrice.getPrice(), lastPrice.getCurrency());
                    return new Advertisement(
                            localRepository.getSektor(advertisementEntity.getSektorName()),
                            building,
                            amount
                    );
                })
                .collect(Collectors.toList());
    }
}
