package de.braincooler.gwhelper.consumer;

import java.util.List;
import java.util.Set;

public class BuildingResponse {
    private int count;
    private List<Building> buildings;
    private Set<String> parsingFailedUrls;

    public BuildingResponse() {
    }

    public BuildingResponse(int count, List<Building> buildings, Set<String> parsingFailedUrls) {
        this.count = count;
        this.buildings = buildings;
        this.parsingFailedUrls = parsingFailedUrls;
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
