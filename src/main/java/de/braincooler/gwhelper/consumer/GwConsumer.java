package de.braincooler.gwhelper.consumer;

import com.gargoylesoftware.htmlunit.html.*;
import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GwConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwConsumer.class);

    private final GwWebClient gwWebClient;
    private final DataRepository dataRepository;

    public GwConsumer(GwWebClient gwWebClient,
                      DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        this.gwWebClient = gwWebClient;
    }

    public void initControlledSektors(int syndId) {
        HtmlTableRow htmlTableRow = gwWebClient.fetchControledSektorsTableRow(syndId);
        DomNodeList<DomNode> childNodes = htmlTableRow.getCells().get(0).getChildNodes();
        for (int i = 1; i < childNodes.size(); i++) {
            Node node = childNodes.get(i).getAttributes().getNamedItem("style");
            String sektorNode = childNodes.get(i).asText();

            if (node != null && node.getNodeValue().contains("color")) {
                String sektorName = sektorNode.substring(sektorNode.indexOf(" ") + 1);
                String color = node.getNodeValue();
                dataRepository.saveControlledSektor(syndId, sektorName, color);
            }
        }
    }

    public void initSyndWarsList(int syndId) {
        HtmlPage htmlPage = gwWebClient.fetchSyndWarsPage(syndId);
        HtmlTable htmlTable = (HtmlTable) htmlPage.getByXPath("/html/body/div[3]/table[3]").get(0);

        for (int i = 0; i < htmlTable.getRows().size(); i++) {
            List<HtmlTableCell> cells = htmlTable.getRow(i).getCells();
            if (cells.size() > 1) {
                String value = htmlTable.getRow(i).getCell(1).asText();
                try {
                    String warSyndId = value.substring(value.indexOf("#") + 1, value.indexOf(" "));
                    dataRepository.saveWar(syndId, Integer.parseInt(warSyndId));
                } catch (Exception ex) {
                    LOGGER.error("error parsing warSyndId: value={}, ex={}", value, ex.getMessage());
                }
            }
        }
    }

    public void initBuildingsFromSektorPage(int sektorX, int sektorY, String type) {
        List<HtmlTableRow> tableRows = gwWebClient.fetchBuildingTable(sektorX, sektorY, type);
        for (int i = 2; i < tableRows.size(); i++) {
            HtmlTableRow row = tableRows.get(i);
            List<HtmlTableCell> cells = row.getCells();
            HtmlTableCell firstCell = cells.get(0);
            String classAttr = firstCell.getAttribute("class");
            if (!classAttr.equals("greenbg") && !classAttr.equals("greengreenbg")) {
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
                    LOGGER.error("error parsing currentControlSyndRef '{}'", currentControlSyndRef);
                }
                int area = 0;
                if (areaRef.contains("(")) {
                    try {
                        area = Integer.parseInt(
                                areaRef.substring(areaRef.indexOf("(") + 1, areaRef.indexOf(")")));
                    } catch (NumberFormatException ex) {
                        LOGGER.error("error parsing areaRef'{}'", areaRef);
                    }
                }

                Building building = new Building();
                building.setArea(area);
                building.setOwnerSynd(ownerSyndId);
                building.setControlSynd(currentControlSyndId);
                String buildingUrl = "http://www.gwars.ru" + objectRef;
                building.setUrl(buildingUrl);
                building.setId(Integer.parseInt(buildingUrl.substring(buildingUrl.indexOf("=") + 1)));
                building.setSektorUrl(String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=%s", sektorX, sektorY, type));
                dataRepository.deleteById(building.getId());

                for (Integer supportedSyndId : dataRepository.getSupportedSyndIds()) {
                    if (dataRepository.hasWar(supportedSyndId, building.getControlSynd()) || building.getStaticControlsyndId() == supportedSyndId) {
                        building.getTargetOfSyndIds().add(supportedSyndId);
                        extendBuildingInfo(building);

                        if (building.getControlSynd() != supportedSyndId &&
                                (building.getDescription() != null && !building.getDescription().contains("Сектор [G]"))) {
                            dataRepository.save(building);
                        } else {
                            dataRepository.deleteById(building.getId());
                        }
                    }
                    if (building.getControlSynd() == supportedSyndId) {
                        dataRepository.saveControlledBuilding(building);
                    } else {
                        dataRepository.deleteControledBuilding(building);
                    }
                }
            }
        }
    }

    private void extendBuildingInfo(Building building) {

        HtmlPage buildingLogPage = gwWebClient.fetchBuildingLogPage(building.getId());
        LocalDateTime readyForAtackTime = readAtackTime(buildingLogPage);
        if (readyForAtackTime.isBefore(LocalDateTime.now(ZoneId.of("Europe/Moscow")))) {
            HtmlPage buildingInfoPage = gwWebClient.fetchBuildingInfoPage(building.getId());
            String buildingInfo = readBuildingInfo(buildingInfoPage);
            String sektorName = readSektorName(buildingInfoPage);
            building.setSektorName(sektorName);
            int staticControlSyndId = 0;
            if (buildingInfo.contains("#")) {
                try {
                    staticControlSyndId = Integer.parseInt(
                            buildingInfo.substring(
                                    buildingInfo.lastIndexOf("#") + 1));
                } catch (NumberFormatException ex) {
                    LOGGER.error("error parsing buildingInfo '{}'", buildingInfo);
                }
            }
            building.setStaticControlsyndId(staticControlSyndId);
            building.setDescription(buildingInfo);
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

    public String readBuildingInfo(HtmlPage buildingPage) {
        HtmlTable table = (HtmlTable) buildingPage.getByXPath("/html/body/div[3]/table[2]/tbody/tr/td/table[1]").get(0);
        String result = table.getRow(0).asText();
        return result == null || result.isEmpty() ? "no value" : result;
    }
}
