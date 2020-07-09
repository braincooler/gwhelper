package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.model.Advertisement;
import de.braincooler.gwhelper.repository.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
public class SiteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteBuilder.class);

    private final AdvertisementRepository advertisementRepository;

    public SiteBuilder(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public String buildSite() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "<head>\n" +
                "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css\">\n" +
                "\t<script type=\"text/javascript\" src=\"https://code.jquery.com/jquery-3.3.1.js\"></script>\n" +
                "\t<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js\"></script>\n" +
                "</head>\n" +

                "<script type=\"text/javascript\">\n" +
                "$(document).ready(function() {\n" +
                "    $('#example thead tr').clone(true).appendTo( '#example thead' );\n" +
                "    $('#example thead tr:eq(1) th').each( function (i) {\n" +
                "        var title = $(this).text();\n" +
                "        $(this).html( '<input type=\"text\" placeholder=\"Search '+title+'\" />' );\n" +
                " \n" +
                "        $( 'input', this ).on( 'keyup change', function () {\n" +
                "            if ( table.column(i).search() !== this.value ) {\n" +
                "                table\n" +
                "                    .column(i)\n" +
                "                    .search( this.value )\n" +
                "                    .draw();\n" +
                "            }\n" +
                "        } );\n" +
                "    } );\n" +
                " \n" +
                "    var table = $('#example').DataTable( {\n" +
                "        iDisplayLength: 50," +
                "        orderCellsTop: true," +
                "        fixedHeader: true," +
                "\"bPaginate\": true,\n" +
                "    \"bLengthChange\": false\n" +

                "    });\n" +
                "});" +
                "</script>\n" +

                "<button onClick=\"window.location.reload();\">Refresh</button>\n </br>" +

                "<table id=\"example\" class=\"display\" style=\"width:100%\">\n" +
                "        <thead>\n" +
                "            <tr>\n" +
                "                <th>Сектор</th>\n" +
                "                <th>Остров</th>\n" +
                "                <th>Тип</th>\n" +
                "                <th>Площадь</th>\n" +
                "                <th>Цена</th>\n" +
                "                <th>Gb/Eun</th>\n" +
                "                <th>Владелец</th>\n" +
                "            </tr>\n" +
                "        </thead>\n" +

                "        <tbody>\n" +
                buildTableBody() +
                "        </tbody>\n" +
                "<tfoot>\n" +
                "            <tr>\n" +
                "                <th>Сектор</th>\n" +
                "                <th>Остров</th>\n" +
                "                <th>Тип</th>\n" +
                "                <th>Площадь</th>\n" +
                "                <th>Цена</th>\n" +
                "                <th>Gb/Eun</th>\n" +
                "                <th>Владелец</th>\n" +
                "            </tr>\n" +
                "        </tfoot>" +
                "    </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildTableBody() {
        StringBuilder body = new StringBuilder();
        for (Advertisement advertisement : advertisementRepository.getAll().values()) {
            body.append(getAsHtmlTr(advertisement));
        }

        return body.toString();
    }

    private String getAsHtmlTr(Advertisement advertisement) {

        String owner = String.format(
                "<a href=\"http://www.gwars.ru/info.php?id=%d\" target=\"_blank\">%s</a>",
                advertisement.getBuilding().getOwnerId(), advertisement.getBuilding().getOwnerName());
        int price = advertisement.getAmount().getPrice();
        String currency = advertisement.getAmount().getCurrency();
        String buildingType = String.format(
                "<a href=\"http://www.gwars.ru/object.php?id=%d\" target=\"_blank\">%s</a>",
                advertisement.getBuilding().getId(), advertisement.getBuilding().getType());

        String sektor = String.format(
                "<a href=\"http://www.gwars.ru/map.php?sx=%d&sy=%d\" target=\"_blank\">%s</a>",
                advertisement.getSektor().getX(), advertisement.getSektor().getY(), advertisement.getSektor().getName());
        return String.format("<tr>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "    <td>%s</td>\n" +
                        "  </tr>",
                sektor,
                advertisement.getSektor().getIsland(),
                buildingType,
                advertisement.getBuilding().getArea(),
                price,
                currency,
                owner
        );
    }

    private String writePretty(int a) {
        String pattern = "###,###.###";
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter.format(a);
    }
}