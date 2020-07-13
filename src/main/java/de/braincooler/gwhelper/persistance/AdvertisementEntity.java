package de.braincooler.gwhelper.persistance;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "advertisement")
public class AdvertisementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "gw_building_id")
    private int gwBuildingId;

    @Column(name = "gw_owner_id")
    private int gwOwnerId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "type")
    private String type;

    @Column(name = "area")
    private Integer area;

    @Column(name = "sektor_name")
    private String sektorName;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "advertisementEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp asc")
    private List<PriceHistoryEntity> priceHistory = new ArrayList<>();

    public AdvertisementEntity() {
    }

    public AdvertisementEntity(Integer id,
                               int gwBuildingId,
                               int gwOwnerId,
                               String ownerName,
                               String type,
                               Integer area,
                               String sektorName,
                               boolean isActive,
                               List<PriceHistoryEntity> priceHistory) {
        this.id = id;
        this.gwBuildingId = gwBuildingId;
        this.gwOwnerId = gwOwnerId;
        this.ownerName = ownerName;
        this.type = type;
        this.area = area;
        this.sektorName = sektorName;
        this.isActive = isActive;
        this.priceHistory = priceHistory;
    }

    public void addHistory(PriceHistoryEntity priceHistoryEntity) {
        priceHistory.add(priceHistoryEntity);
        priceHistoryEntity.setAdvertisementEntity(this);
    }

    public void removeHistory(PriceHistoryEntity priceHistoryEntity) {
        priceHistory.remove(priceHistoryEntity);
        priceHistoryEntity.setAdvertisementEntity(null);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getGwBuildingId() {
        return gwBuildingId;
    }

    public void setGwBuildingId(int gwBuildingId) {
        this.gwBuildingId = gwBuildingId;
    }

    public int getGwOwnerId() {
        return gwOwnerId;
    }

    public void setGwOwnerId(int gwOwnerId) {
        this.gwOwnerId = gwOwnerId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<PriceHistoryEntity> getPriceHistory() {
        return priceHistory;
    }

    public void setPriceHistory(List<PriceHistoryEntity> priceHistory) {
        this.priceHistory = priceHistory;
    }
}
