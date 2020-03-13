package de.braincooler.gwhelper.consumer;

import java.util.List;

public class BuildingResponse {
    private int count;
    private List<Building> buildings;

    public BuildingResponse() {
    }

    public BuildingResponse(int count, List<Building> buildings) {
        this.count = count;
        this.buildings = buildings;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }
}
