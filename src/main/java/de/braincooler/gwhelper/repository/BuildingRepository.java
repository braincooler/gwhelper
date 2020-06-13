package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.Building;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class BuildingRepository {
    private final Set<Building> buildingList;

    public BuildingRepository() {
        this.buildingList = new HashSet<>();
    }

    public void save(Building building) {
        buildingList.add(building);
    }

    public void deleteById(int id) {
        buildingList.removeIf(building -> building.getId() == id);
    }

    public List<Building> findByTargetOfSyndId(int syndId) {
        return buildingList.stream()
                .filter(building -> building.getTargetOfSyndIds().contains(syndId))
                .collect(Collectors.toList());
    }
}
