package de.braincooler.gwhelper.model;

public class Building {
    private int id;
    private String type;
    private int area;
    private int ownerId;
    private String ownerName;

    public Building(int id, String type, int area, int ownerId, String ownerName) {
        this.id = id;
        this.type = type;
        this.area = area;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
