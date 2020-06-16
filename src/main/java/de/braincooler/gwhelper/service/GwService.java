package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.Building;
import de.braincooler.gwhelper.consumer.GwConsumer;
import de.braincooler.gwhelper.consumer.SiteBuilder;
import de.braincooler.gwhelper.repository.DataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class GwService {
    private final DataRepository dataRepository;
    private final SiteBuilder siteBuilder;
    private final GwConsumer gwConsumer;

    public GwService(SiteBuilder siteBuilder,
                     DataRepository dataRepository, GwConsumer gwConsumer) {
        this.siteBuilder = siteBuilder;
        this.dataRepository = dataRepository;
        this.gwConsumer = gwConsumer;
    }

    public String getBuildingsWithoutTurel(int syndId) {
        List<Building> buildingsWihtoutTurel = dataRepository.findByTargetOfSyndId(syndId).stream()
                .filter(building -> building.getControlSynd() != building.getStaticControlsyndId())
                .collect(Collectors.toList());

        return siteBuilder.buildSite(buildingsWihtoutTurel, syndId);
    }

    public Set<Integer> getWarlist(int syndId) {
        return dataRepository.getWarlist(syndId);
    }

    public void initSektor(int sektorX, int sektorY) {
        gwConsumer.initBuildingsFromSektorPage(sektorX, sektorY, "tech");

        //gwConsumer.initBuildingsFromSektorPage(sektorX, sektorY, "tech");
    }

    public Set<Building> getAll() {
        return dataRepository.getAllBuildings();
    }

    public Map<String, String> getControlledSektors(int syndId) {
        return dataRepository.getControlledSektors(syndId);
    }
}
