package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.consumer.Building;
import de.braincooler.gwhelper.consumer.BuildingResponse;
import de.braincooler.gwhelper.consumer.GwConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class GwService {
    private static final String HTML_LINK = "<a href=\"%s\" target=\"_blank\">%s</a>";
    private static final Set<Integer> SYND_INC = new HashSet<>(Arrays.asList(15));
    private static List<Integer> enemySyndTop = Arrays.asList(6363, 592, 5353, 1677);

    private final GwConsumer gwConsumer;
    // link - syndId
    private Map<String, Integer> targets;
    private Map<String, Integer> targetsWithoutTurrel;
    private List<Building> buildingsReadyForAttack = new ArrayList<>();

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

    public BuildingResponse getBuildingsReadyForAttack() {
        return new BuildingResponse(buildingsReadyForAttack.size(), buildingsReadyForAttack);
    }

    public BuildingResponse getBuildingsReadyForAttackNoTop() {
        List<Building> buildingsNoTop = buildingsReadyForAttack.stream()
                .filter(building -> !enemySyndTop.contains(building.getControlSynd()))
                .collect(Collectors.toList());
        return new BuildingResponse(buildingsNoTop.size(), buildingsNoTop);
    }

    @Scheduled(fixedDelay = 1200000, initialDelay = 5000) // 20 min
    private void initBuildingsReadyForAttack() {
        List<Building> sektorBuilings = gwConsumer.getSektorBuilings().stream()
                .filter(building -> building.getControlSynd() != building.getStaticControlsyndId())
                .collect(Collectors.toList());
        buildingsReadyForAttack = sektorBuilings.stream()
                .filter(building -> {
                    LocalDateTime nextAtackTime = getAtackTime(building.getId());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return nextAtackTime.isBefore(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
                })
                .collect(Collectors.toList());
    }

    public Set<String> getLogs() {
        return gwConsumer.getNotReadablePages();
    }

    public LocalDateTime getAtackTime(int buildingId) {
        return gwConsumer.getAtackTime(buildingId);
    }

    public int getStaticControlSyndId(int buildingId) {
        return gwConsumer.getStaticControlSyndId(buildingId);
    }
}
