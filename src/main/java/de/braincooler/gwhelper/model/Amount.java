package de.braincooler.gwhelper.model;

public class Amount {
    private int price;
    private String currency;

    public Amount(int price, String currency) {
        this.price = price;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
