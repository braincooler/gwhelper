package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.consumer.BuildingRepository;
import de.braincooler.gwhelper.consumer.GwConsumer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GwService {
    private final GwConsumer gwConsumer;
    private final BuildingRepository buildingRepository;

    public GwService(GwConsumer gwConsumer, BuildingRepository buildingRepository) {
        this.gwConsumer = gwConsumer;
        this.buildingRepository = buildingRepository;
    }

    public String getTargetLinks() {
        AtomicReference<String> resultBody = new AtomicReference<>("");
        buildingRepository.findAll().forEach(building -> {
            resultBody.set(resultBody + building.getAsHtmlTr());
        });

        return getHtmlSite(resultBody.get());
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 1000) // 20 min
    private void initBuildingsReadyForAttack() {
        gwConsumer.initSektorObjects();
    }

    public Set<String> getLogs() {
        return gwConsumer.getNotReadablePages();
    }

    private String getHtmlSite(String body) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title>#1635</title>\n" +
                "<style>\n" +
                "table {\n" +
                "  border-spacing: 0;\n" +
                "  width: 100%;\n" +
                "  border: 1px solid #ddd;\n" +
                "}\n" +
                "th {\n" +
                "  cursor: pointer;\n" +
                "}\n" +
                "th, td {\n" +
                "  text-align: left;\n" +
                "  padding: 16px;\n" +
                "}\n" +
                "tr:nth-child(even) {\n" +
                "  background-color: #f2f2f2\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table id=\"myTable\">\n" +
                "  <tr>  \n" +
                "    <th onclick=\"sortTable(0)\">Недвижимость</th>\n" +
                "    <th onclick=\"sortTable(1)\">Cиндикат</th>\n" +
                "    <th onclick=\"sortTable(2)\">Площадь</th>\n" +
                "    <th onclick=\"sortTable(3)\">Сектор</th>\n" +
                "  </tr>\n" +
                body +
                "</table>\n" +
                "<script>\n" +
                "function sortTable(n) {\n" +
                "  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;\n" +
                "  table = document.getElementById(\"myTable\");\n" +
                "  switching = true;\n" +
                "  dir = \"asc\"; \n" +
                "  while (switching) {\n" +
                "    switching = false;\n" +
                "    rows = table.rows;\n" +
                "    for (i = 1; i < (rows.length - 1); i++) {\n" +
                "      shouldSwitch = false;\n" +
                "      x = rows[i].getElementsByTagName(\"TD\")[n];\n" +
                "      y = rows[i + 1].getElementsByTagName(\"TD\")[n];\n" +
                "      if (dir == \"asc\") {\n" +
                "        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {\n" +
                "          shouldSwitch= true;\n" +
                "          break;\n" +
                "        }\n" +
                "      } else if (dir == \"desc\") {\n" +
                "        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {\n" +
                "          shouldSwitch = true;\n" +
                "          break;\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    if (shouldSwitch) {\n" +
                "      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);\n" +
                "      switching = true;\n" +
                "      switchcount ++;      \n" +
                "    } else {\n" +
                "      \n" +
                "      if (switchcount == 0 && dir == \"asc\") {\n" +
                "        dir = \"desc\";\n" +
                "        switching = true;\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>\n";
    }
}
