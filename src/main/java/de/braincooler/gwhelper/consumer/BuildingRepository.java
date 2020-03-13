package de.braincooler.gwhelper.consumer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BuildingRepository {

    private Map<Integer, Building> buildings;

    public BuildingRepository() {
        this.buildings = new HashMap<>();
    }

    public void save(Building building) {
        buildings.put(building.getId(), building);
    }

    public void delete(Building building) {
        buildings.remove(building.getId());
    }

    public List<Building> findAll() {
        return new ArrayList<>(buildings.values());
    }

    public Building findById(int id) {
        return buildings.get(id);
    }
}
