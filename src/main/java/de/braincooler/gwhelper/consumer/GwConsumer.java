package de.braincooler.gwhelper.consumer;

import com.gargoylesoftware.htmlunit.html.*;
import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.repository.BuildingEntity;
import de.braincooler.gwhelper.repository.BuildingMapper;
import de.braincooler.gwhelper.repository.BuildingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GwConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GwConsumer.class);
    private static final List<Integer> supportedSynds = Arrays.asList(1635, 1637);

    private Map<Integer, Set<Integer>> enemySynd;
    private Map<Integer, Map<String, String>> controlledSektors;

    private final GwWebClient gwWebClient;
    private final BuildingRepository buildingRepository;

    public GwConsumer(GwWebClient gwWebClient,
                      BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
        controlledSektors = new HashMap<>();
        enemySynd = new HashMap<>();
        this.gwWebClient = gwWebClient;
    }

    public Map<String, String> getControlledSektors(int syndId) {
        return new HashMap<>(controlledSektors.get(syndId));
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
                Map<String, String> controlledSektorsMap = controlledSektors.computeIfAbsent(syndId, k -> new HashMap<>());
                controlledSektorsMap.put(sektorName, color);
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
                    Set<Integer> syndWarSet = enemySynd.computeIfAbsent(syndId, k -> new HashSet<>());
                    syndWarSet.add(Integer.parseInt(warSyndId));
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
                String buildingUrl = "http://www.gwars.ru" + objectRef;
                building.setUrl(buildingUrl);
                building.setId(Integer.parseInt(buildingUrl.substring(buildingUrl.indexOf("=") + 1)));
                buildingRepository.deleteById(building.getId());

                for (Integer syndId : supportedSynds) {
                    building.getTargetOfSyndIds().add(syndId);
                    if (enemySynd.get(syndId).contains(currentControlSyndId) || ownerSyndId == syndId) {
                        HtmlPage buildingLogPage = gwWebClient.fetchBuildingLogPage(building.getId());
                        LocalDateTime readyForAtackTime = readAtackTime(buildingLogPage);
                        if (readyForAtackTime.isBefore(LocalDateTime.now(ZoneId.of("Europe/Moscow")))) {
                            HtmlPage buildingInfoPage = gwWebClient.fetchBuildingInfoPage(building.getId());
                            String buildingInfo = readBuildingInfo(buildingInfoPage);
                            String sektorName = readSektorName(buildingInfoPage);
                            building.setSektorName(sektorName);
                            building.setOwnerSynd(ownerSyndId);
                            building.setControlSynd(currentControlSyndId);
                            building.setSektorUrl(String.format("http://www.gwars.ru/map.php?sx=%d&sy=%d&st=%s", sektorX, sektorY, type));
                            building.setArea(area);

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

                            if (currentControlSyndId != syndId && (!buildingInfo.contains("Сектор [G]") ||
                                    building.getOwnerSynd() == 15)) {
                                BuildingEntity buildingEntity = BuildingMapper.toEntity(building);
                                buildingEntity.setUpdateTimestamp(Instant.now().getEpochSecond());
                                buildingRepository.save(building);
                            } else {
                                buildingRepository.deleteById(building.getId());

                            }
                        }
                    }
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
