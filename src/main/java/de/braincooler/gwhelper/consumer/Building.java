package de.braincooler.gwhelper.consumer;

public class Building {
    private int ownerSynd;
    private int controlSynd;
    private String ref;
    private int area;
    private String sektorUrl;
    private int staticControlsyndId;
    private String description;


    public String getAsHtmlTr() {
        String syndSignLinkTemplate = "<img src=\"https://images.gwars.ru/img/synds/%s.gif\" width=\"20\" height=\"14\" border=\"0\" class=\"usersign\" title=\"#%s\">";
        String linkTemplate = "<a href=\"%s\" target=\"_blank\">%s</a>";
        String buildingLink = String.format(linkTemplate, ref, description);
        String controlSyndOnlineLink = String.format("http://www.gwars.ru/syndicate.php?id=%d&page=online", controlSynd);
        String constrolSindLink = String.format(linkTemplate,
                controlSyndOnlineLink,
                String.format(syndSignLinkTemplate, controlSynd, controlSynd));
        return String.format("<tr>\n" +
                "    <td>%s</td>\n" +
                "    <td>%s</td>\n" +
                "    <td>%d</td>\n" +
                "  </tr>", buildingLink, constrolSindLink, area
        );
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
