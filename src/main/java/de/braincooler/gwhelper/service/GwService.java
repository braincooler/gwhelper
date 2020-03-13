package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.consumer.Building;
import de.braincooler.gwhelper.consumer.BuildingResponse;
import de.braincooler.gwhelper.consumer.GwConsumer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class GwService {
    private static final String HTML_LINK = "<a href=\"%s\" target=\"_blank\">%s</a>";
    private static final Set<Integer> SYND_INC = new HashSet<>(Arrays.asList(15));

    private final GwConsumer gwConsumer;
    // link - syndId
    private Map<String, Integer> targets;
    private Map<String, Integer> targetsWithoutTurrel;

    public GwService(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
        targets = new HashMap<>();
        targetsWithoutTurrel = new HashMap<>();
    }

    //@Scheduled(fixedDelay = 620000)
    public void updateTargets() {
        targets = gwConsumer.getMapTargetBuildingAndSyndId();
        targetsWithoutTurrel.clear();
        targets.keySet().forEach(link -> {
            int ownerSyndicate = gwConsumer.getBuildingOwnerSyndicateId(link);
            if (SYND_INC.contains(ownerSyndicate) || ownerSyndicate != targets.get(link)) {
                targetsWithoutTurrel.put(link, targets.get(link));
            }
        });
    }

    public String getTargetLinks() {
        AtomicReference<String> resultBody = new AtomicReference<>("");
        targetsWithoutTurrel.keySet().forEach(link -> {
            resultBody.set(resultBody + String.format(HTML_LINK, link, targets.get(link)) + "</br>");
        });

        return getHtmlSite(resultBody.get());
    }

    private String getHtmlSite(String body) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>1635</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h3>" +
                "\n" + body +
                "</h3>" +
                "</body>\n" +
                "</html>";
    }

    public BuildingResponse getSektorObject() {
        List<Building> sektorBuilings = gwConsumer.getSektorBuilings().stream()
                .filter(building -> building.getControlSynd() != building.getOwnerSynd())
                .collect(Collectors.toList());
        return new BuildingResponse(sektorBuilings.size(), sektorBuilings);
    }

    public Map<String, String> getLogs() {
        return gwConsumer.getLogs();
    }

    public String getAtackTime(int buildingId) {
        return gwConsumer.getAtackTime(buildingId);
    }
}
