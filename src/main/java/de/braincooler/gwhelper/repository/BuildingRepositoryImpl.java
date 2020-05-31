package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.Building;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BuildingRepositoryImpl implements BuildingRepository {
    private final Map<Integer, Building> buildings;

    public BuildingRepositoryImpl() {
        this.buildings = new HashMap<>();
    }

    @Override
    public void save(Building building) {
        buildings.put(building.getId(), building);
    }

    @Override
    public void delete(Building building) {
        buildings.remove(building.getId());
    }

    @Override
    public List<Building> findAll() {
        return new ArrayList<>(buildings.values());
    }

    @Override
    public Building findById(int id) {
        return buildings.get(id);
    }
}
