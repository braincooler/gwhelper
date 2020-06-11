package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.consumer.SiteBuilder;
import de.braincooler.gwhelper.repository.BuildingJpaRepository;
import de.braincooler.gwhelper.repository.BuildingMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GwService {
    private final BuildingJpaRepository buildingJpaRepository;
    private final SiteBuilder siteBuilder;

    public GwService(SiteBuilder siteBuilder,
                     BuildingJpaRepository buildingJpaRepository) {
        this.siteBuilder = siteBuilder;
        this.buildingJpaRepository = buildingJpaRepository;
    }

    public String getBuildingsWithoutTurel(int syndId) {
        Long timestamp = Instant.now().minusSeconds(30 * 60).getEpochSecond();
        List<Building> buildingsWihtoutTurel = buildingJpaRepository
                .findByUpdateTimestampGreaterThanAndTargetOfSyndIdIs(timestamp, syndId).stream()
                .filter(buildingEntity -> buildingEntity.getControlSyndId() != buildingEntity.getStaticControlSyndId())
                .map(BuildingMapper::toDto)
                .collect(Collectors.toList());

        return siteBuilder.buildSite(buildingsWihtoutTurel);
    }
}
