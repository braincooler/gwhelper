package de.braincooler.gwhelper.persistance;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "building")
public class BuildingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "gw_id")
    private int gwId;

    @Column(name = "owner_id")
    private int ownerId;

    @Column(name = "sektor_name")
    private String sektorName;

    @Column(name = "is_on_sale")
    private boolean isOnSale;

    @OneToMany(mappedBy = "buildingEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp asc")
    private List<PriceHistoryEntity> priceHistory = new ArrayList<>();

    public BuildingEntity() {
    }

    public BuildingEntity(Integer id,
                          int gwId,
                          int ownerId,
                          String sektorName,
                          boolean isOnSale,
                          List<PriceHistoryEntity> priceHistory) {
        this.id = id;
        this.gwId = gwId;
        this.ownerId = ownerId;
        this.sektorName = sektorName;
        this.isOnSale = isOnSale;
        this.priceHistory = priceHistory;
    }

    public void addHistory(PriceHistoryEntity priceHistoryEntity) {
        priceHistory.add(priceHistoryEntity);
        priceHistoryEntity.setBuildingEntity(this);
    }

    public void removeHistory(PriceHistoryEntity priceHistoryEntity) {
        priceHistory.remove(priceHistoryEntity);
        priceHistoryEntity.setBuildingEntity(null);
    }

    public String getSektorName() {
        return sektorName;
    }

    public void setSektorName(String sektorName) {
        this.sektorName = sektorName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getGwId() {
        return gwId;
    }

    public void setGwId(int gwId) {
        this.gwId = gwId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOnSale() {
        return isOnSale;
    }

    public void setOnSale(boolean onSale) {
        isOnSale = onSale;
    }

    public List<PriceHistoryEntity> getPriceHistory() {
        return priceHistory;
    }

    public void setPriceHistory(List<PriceHistoryEntity> priceHistory) {
        this.priceHistory = priceHistory;
    }
}
