package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.consumer.SiteBuilder;
import de.braincooler.gwhelper.repository.BuildingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class GwService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwService.class);

    private final BuildingRepository buildingRepository;
    private final SiteBuilder siteBuilder;

    public GwService(SiteBuilder siteBuilder,
                     BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
        this.siteBuilder = siteBuilder;
    }

    public String getBuildingsWithoutTurel() {
        List<Building> buildingsWihtoutTurel = buildingRepository.findAll().stream()
                .filter(building -> building.getControlSynd() != building.getStaticControlsyndId())
                .collect(Collectors.toList());

        return siteBuilder.buildSite(buildingsWihtoutTurel);
    }
}
