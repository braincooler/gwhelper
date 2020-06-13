package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.consumer.SiteBuilder;
import de.braincooler.gwhelper.repository.BuildingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class GwService {
    private final BuildingRepository buildingRepository;
    private final SiteBuilder siteBuilder;

    public GwService(SiteBuilder siteBuilder,
                     BuildingRepository buildingRepository) {
        this.siteBuilder = siteBuilder;
        this.buildingRepository = buildingRepository;
    }

    public String getBuildingsWithoutTurel(int syndId) {
        List<Building> buildingsWihtoutTurel = buildingRepository
                .findByTargetOfSyndId(syndId).stream()
                .filter(building -> building.getControlSynd() != building.getStaticControlsyndId())
                .collect(Collectors.toList());

        return siteBuilder.buildSite(buildingsWihtoutTurel);
    }
}
