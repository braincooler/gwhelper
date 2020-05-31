package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.Building;

import java.util.List;

public interface BuildingRepository {
    void save(Building building);

    void delete(Building building);

    List<Building> findAll();

    Building findById(int id);
}
