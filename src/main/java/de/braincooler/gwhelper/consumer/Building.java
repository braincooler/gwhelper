package de.braincooler.gwhelper.consumer;

public class Building {
    private int ownerSynd;
    private int controlSynd;
    private String ref;
    private int area;
    private String sektorUrl;
    private int staticControlsyndId;

    public int getStaticControlsyndId() {
        return staticControlsyndId;
    }

    public void setStaticControlsyndId(int staticControlsyndId) {
        this.staticControlsyndId = staticControlsyndId;
    }

    public int getId() {
        return Integer.parseInt(ref.substring(ref.indexOf("=") + 1));
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

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }
}
