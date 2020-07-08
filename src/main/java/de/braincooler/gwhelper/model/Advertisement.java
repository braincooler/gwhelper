package de.braincooler.gwhelper.model;

public class Advertisement {
    private Sektor sektor;
    private Building building;
    private Amount amount;

    public Advertisement(Sektor sektor, Building building, Amount amount) {
        this.sektor = sektor;
        this.building = building;
        this.amount = amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public Sektor getSektor() {
        return sektor;
    }

    public void setSektor(Sektor sektor) {
        this.sektor = sektor;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Amount getAmount() {
        return amount;
    }
}
