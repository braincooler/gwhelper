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

    public String getTargetsWithoutTurel() {
        AtomicReference<String> trBody = new AtomicReference<>("");
        buildingRepository.findAll().forEach(building -> {
            if (building.getStaticControlsyndId() == 0) {
                if (building.getOwnerSynd() != building.getControlSynd()) {
                    trBody.set(trBody + building.getAsHtmlTr());
                }
            } else if (building.getControlSynd() != building.getStaticControlsyndId()) {
                trBody.set(trBody + building.getAsHtmlTr());
            }
        });

        return buildHtmlSite(trBody.get());
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 1000) // 20 min
    private void initBuildingsReadyForAttack() {
        gwConsumer.initSektorObjects();
    }

    public Set<String> getLogs() {
        return gwConsumer.getNotReadablePages();
    }

    private String buildHtmlSite(String body) {
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
                "                <th>Cиндикат</th>\n" +
                "                <th>Площадь</th>\n" +
                "                <th>Сектор</th>\n" +
                "                <th>Турель</th>\n" +
                "            </tr>\n" +
                "        </thead>\n" +
                "        <tbody>\n" +
                body +
                "        </tbody>\n" +
                "    </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    public Object getTest() {
        return null;
    }
}
