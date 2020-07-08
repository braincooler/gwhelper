package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.model.Advertisement;
import de.braincooler.gwhelper.model.Sektor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AdvertisementRepository {
    private final Map<Integer, Advertisement> advertisementMap = new HashMap<>();

    public void addAdvertisement(Advertisement advertisement) {
        advertisementMap.put(advertisement.getBuilding().getId(), advertisement);
    }

    public void removeOld(Sektor sektor) {
        advertisementMap.values().removeIf(advertisement -> advertisement.getSektor().equals(sektor));
    }

    public Map<Integer, Advertisement> getAll() {
        return advertisementMap;
    }
}
