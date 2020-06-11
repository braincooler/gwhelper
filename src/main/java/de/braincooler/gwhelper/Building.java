package de.braincooler.gwhelper;

public class Building {

    private int id;
    private int ownerSynd;
    private int controlSynd;
    private String url;
    private int area;
    private String sektorUrl;
    private int staticControlsyndId;
    private String description;
    private String sektorName;
    private int targetOfSyndId;

    public int getTargetOfSyndId() {
        return targetOfSyndId;
    }

    public void setTargetOfSyndId(int targetOfSyndId) {
        this.targetOfSyndId = targetOfSyndId;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getStaticControlsyndId() {
        return staticControlsyndId;
    }

    public void setStaticControlsyndId(int staticControlsyndId) {
        this.staticControlsyndId = staticControlsyndId;
    }

    public int getId() {
        return id;
        //return Integer.parseInt(url.substring(url.indexOf("=") + 1));
    }

    public String getSektorUrl() {
        return sektorUrl;
    }

    public void setSektorUrl(String sektorUrl) {
        this.sektorUrl = sektorUrl;
    }

    public int getOwnerSynd() {
        return ownerSynd;
    }

    public void setOwnerSynd(int ownerSynd) {
        this.ownerSynd = ownerSynd;
    }

    public int getControlSynd() {
        return controlSynd;
    }

    public void setControlSynd(int controlSynd) {
        this.controlSynd = controlSynd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }
}
