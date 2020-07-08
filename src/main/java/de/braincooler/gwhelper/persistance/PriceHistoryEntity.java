package de.braincooler.gwhelper.persistance;

import javax.persistence.*;

@Entity
@Table(name = "price_history")
public class PriceHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "price")
    private Integer price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "timestamp")
    private Long timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    private BuildingEntity buildingEntity;

    public PriceHistoryEntity() {
    }

    public PriceHistoryEntity(Integer id, Integer price, String currency, Long timestamp, BuildingEntity buildingEntity) {
        this.id = id;
        this.price = price;
        this.currency = currency;
        this.timestamp = timestamp;
        this.buildingEntity = buildingEntity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BuildingEntity getBuildingEntity() {
        return buildingEntity;
    }

    public void setBuildingEntity(BuildingEntity buildingEntity) {
        this.buildingEntity = buildingEntity;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceHistoryEntity)) return false;
        return id != null && id.equals(((PriceHistoryEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
