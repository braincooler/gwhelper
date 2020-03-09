package de.braincooler.gwhelper.controller;

import de.braincooler.gwhelper.consumer.GwConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/")
public class GwController {

    private final GwConsumer gwConsumer;

    public GwController(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
    }

    @GetMapping
    public ResponseEntity<?> getTargets() {
        Map<String, Integer> targetStrings = gwConsumer.get1635TargetStrings();
        String link = "<a href=\"%s\">%s</a>\n";
        AtomicReference<String> resultBody = new AtomicReference<>("");
        targetStrings.keySet().forEach(s -> {
            int ownerSindikat = gwConsumer.getOwnerSindikat(s);
            if (ownerSindikat == 15 || ownerSindikat != targetStrings.get(s)) {
                resultBody.set(resultBody +
                        String.format(link, "http://www.gwars.ru" + s, targetStrings.get(s)) +
                        "</br>");
            }
        });


        String template = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" + resultBody +
                "</body>\n" +
                "</html>";
        return ResponseEntity.ok(template);
    }
}
