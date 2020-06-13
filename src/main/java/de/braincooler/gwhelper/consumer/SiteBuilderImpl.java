package de.braincooler.gwhelper.consumer;

import de.braincooler.gwhelper.Building;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SiteBuilderImpl implements SiteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteBuilderImpl.class);

    private final GwConsumer gwConsumer;

    public SiteBuilderImpl(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
    }

    @Override
    public String buildSite(List<Building> buildings) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "<head>\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\">\n" +
                "\t<script type=\"text/javascript\" src=\"https://code.jquery.com/jquery-3.3.1.js\"></script>\n" +
                "\t<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script>\n" +
                "</head>\n" +

                "<script type=\"text/javascript\">\n" +
                "\t$(document).ready(function() {\n" +
                "    $('#example').DataTable( {\n" +
                "\"iDisplayLength\": 50," +
                "        columnDefs: [ {\n" +
                "            targets: [ 0 ],\n" +
                "            orderData: [ 0, 1 ]\n" +
                "        }, {\n" +
                "            targets: [ 1 ],\n" +
                "            orderData: [ 1, 0 ]\n" +
                "        }, {\n" +
                "            targets: [ 4 ],\n" +
                "            orderData: [ 4, 0 ]\n" +
                "        } ]\n" +
                "    } );\n" +
                "} );\n" +
                "</script>\n" +

                "<button onClick=\"window.location.reload();\">Refresh</button>\n </br>" +

                "<table id=\"example\" class=\"display\" style=\"width:100%\">\n" +
                "        <thead>\n" +
                "            <tr>\n" +
                "                <th>Недвижимость</th>\n" +
                "                <th>Владелец</th>\n" +
                "                <th>Cиндикат</th>\n" +
                "                <th>Площадь</th>\n" +
                "                <th>Сектор</th>\n" +
                "                <th>Личка</th>\n" +
                "            </tr>\n" +
                "        </thead>\n" +
                "        <tbody>\n" +
                buildTableBody(buildings) +
                "        </tbody>\n" +
                "    </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildTableBody(List<Building> buildings) {
        AtomicReference<String> tableBody = new AtomicReference<>("");
        buildings.forEach(building -> tableBody.set(tableBody + getAsHtmlTr(building)));

        return tableBody.get();
    }

    private String getAsHtmlTr(Building building) {
        String syndSignLinkTemplate = "<img src=\"https://images.gwars.ru/img/synds/%s.gif\"" +
                " width=\"20\" height=\"14\" border=\"0\" class=\"usersign\" title=\"#%s\">";
        String linkTemplate = "<a href=\"%s\" target=\"_blank\">%s</a>";

        String buildingLink = String.format(
                linkTemplate,
                building.getUrl(),
                building.getDescription().substring(0, building.getDescription().indexOf("(") - 1));

        String controlSyndOnlineLink = String.format(
                "http://www.gwars.ru/syndicate.php?id=%d&page=online",
                building.getControlSynd());

        String constrolSindLink = String.format(
                linkTemplate,
                controlSyndOnlineLink,
                String.format(syndSignLinkTemplate, building.getControlSynd(), building.getControlSynd()));
        String color = "color:#00000";
        //    if (gwConsumer.getControlledSektors(building.getTargetOfSyndId()).containsKey(building.getSektorName())) {
        //        color = gwConsumer.getControlledSektors(building.getTargetOfSyndId()).get(building.getSektorName());
        //    }
        String style = String.format("style=%s", color);


        String owner = "";
        try {
            if (building.getDescription().contains("#")) {
                owner = building.getDescription().substring(building.getDescription().indexOf(",") + 2, building.getDescription().indexOf("#") - 1);
            } else {
                owner = building.getDescription().substring(building.getDescription().indexOf(",") + 2);
            }
        } catch (Exception ex) {
            LOGGER.error("siteBuilderImpl: error parsing owner: {}", building.getDescription());
        }
        return String.format("<tr>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%d</td>\n" +
                        "    <td %s>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "  </tr>",
                buildingLink,
                owner,
                constrolSindLink + String.format(
                        "<a href=\"http://www.gwars.ru/syndicate.php?id=%d\">%d</a>",
                        building.getControlSynd(),
                        building.getControlSynd()),
                building.getArea(),
                style, building.getSektorName(),
                building.getStaticControlsyndId() == 1635 ? "+" : "-"
        );
    }
}
