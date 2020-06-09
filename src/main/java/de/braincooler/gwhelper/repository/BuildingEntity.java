package de.braincooler.gwhelper.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "building")
public class BuildingEntity {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "ownerSyndid")
    private int ownerSyndId;

    @Column(name = "constrolSyndId")
    private int controlSyndId;

    @Column(name = "staticControlSyndId")
    private int staticControlSyndId;

    @Column(name = "area")
    private int area;

    @Column(name = "sektorName")
    private String sektorName;

    @Column(name = "sektorUrl")
    private String sektorUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "nextAttackTimestamp")
    private Long nextAttackTimestamp;

    @Column(name = "updateTimestamp")
    private Long updateTimestamp;

    public BuildingEntity(int id,
                          int ownerSyndId,
                          int controlSyndId,
                          int staticControlSyndId,
                          int area,
                          String sektorName,
                          String sektorUrl,
                          String description) {
        this.id = id;
        this.ownerSyndId = ownerSyndId;
        this.controlSyndId = controlSyndId;
        this.staticControlSyndId = staticControlSyndId;
        this.area = area;
        this.sektorName = sektorName;
        this.sektorUrl = sektorUrl;
        this.description = description;
    }

    public String getSektorUrl() {
        return sektorUrl;
    }

    public void setSektorUrl(String sektorUrl) {
        this.sektorUrl = sektorUrl;
    }

    public Long getNextAttackTimestamp() {
        return nextAttackTimestamp;
    }

    public void setNextAttackTimestamp(Long nextAttackTimestamp) {
        this.nextAttackTimestamp = nextAttackTimestamp;
    }

    public Long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerSyndId() {
        return ownerSyndId;
    }

    public void setOwnerSyndId(int ownerSyndId) {
        this.ownerSyndId = ownerSyndId;
    }

    public int getControlSyndId() {
        return controlSyndId;
    }

    public void setControlSyndId(int controlSyndId) {
        this.controlSyndId = controlSyndId;
    }

    public int getStaticControlSyndId() {
        return staticControlSyndId;
    }

    public void setStaticControlSyndId(int staticControlSyndId) {
        this.staticControlSyndId = staticControlSyndId;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public String getSektorName() {
        return sektorName;
    }

    public void setSektorName(String sektorName) {
        this.sektorName = sektorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
