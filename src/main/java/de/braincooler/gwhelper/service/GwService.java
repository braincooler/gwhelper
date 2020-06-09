package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.consumer.SiteBuilder;
import de.braincooler.gwhelper.repository.BuildingJpaRepository;
import de.braincooler.gwhelper.repository.BuildingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GwService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GwService.class);

    //private final BuildingRepository buildingRepository;
    private final BuildingJpaRepository buildingJpaRepository;
    private final SiteBuilder siteBuilder;

    public GwService(SiteBuilder siteBuilder,
                     //BuildingRepository buildingRepository,
                     BuildingJpaRepository buildingJpaRepository) {
        //this.buildingRepository = buildingRepository;
        this.siteBuilder = siteBuilder;
        this.buildingJpaRepository = buildingJpaRepository;
    }

    public String getBuildingsWithoutTurel() {
        Long timestamp = Instant.now().minusSeconds(30 * 60).getEpochSecond();
        List<Building> buildingsWihtoutTurel = buildingJpaRepository.findByUpdateTimestampGreaterThan(timestamp).stream()
                .filter(buildingEntity -> buildingEntity.getControlSyndId() != buildingEntity.getStaticControlSyndId())
                .map(BuildingMapper::toDto)
                .collect(Collectors.toList());

        return siteBuilder.buildSite(buildingsWihtoutTurel);
    }
}
