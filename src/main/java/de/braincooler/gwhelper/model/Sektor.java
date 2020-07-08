package de.braincooler.gwhelper.model;

public class Sektor {
    private String island;
    private String name;
    private int x;
    private int y;

    public Sektor(String island, String name, int x, int y) {
        this.island = island;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getIsland() {
        return island;
    }

    public void setIsland(String island) {
        this.island = island;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
