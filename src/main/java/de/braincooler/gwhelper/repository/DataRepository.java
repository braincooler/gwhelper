package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.Building;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DataRepository {
    private final Set<Building> buildingList;
    private final Map<Integer, Set<Integer>> warSynd;
    private final Map<Integer, Map<String, String>> controlledSektors;

    public DataRepository() {
        this.buildingList = new HashSet<>();
        this.controlledSektors = new HashMap<>();
        this.warSynd = new HashMap<>();
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

    public void saveControlledSektor(int syndId, String sektorName, String color) {
        Map<String, String> controlledSektorsMap = controlledSektors.computeIfAbsent(syndId, k -> new HashMap<>());
        controlledSektorsMap.put(sektorName, color);
    }

    public boolean hasWar(Integer syndId, int currentControlSyndId) {
        return warSynd.get(syndId).contains(currentControlSyndId);
    }

    public void saveWar(int syndId, int warSyndId) {
        Set<Integer> syndWarSet = warSynd.computeIfAbsent(syndId, k -> new HashSet<>());
        syndWarSet.add(warSyndId);
    }

    public Set<Integer> getWarlist(int syndId) {
        return warSynd.get(syndId);
    }

    public Set<Building> getAllBuildings() {
        return buildingList;
    }
}
