package de.braincooler.gwhelper.controller;

import de.braincooler.gwhelper.consumer.GwConsumer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class GwController {

    private final GwConsumer gwConsumer;

    public GwController(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
    }

    public ResponseEntity<?> getTargets() {
        Map<String, String> result = new HashMap<>();
        Map<String, Integer> targetStrings = gwConsumer.get1635TargetStrings();
        targetStrings.keySet().forEach(s -> {
            String link = "http://www.gwars.ru" + s;
            int ownerSindikat = gwConsumer.getOwnerSindikat(s);
            if (ownerSindikat == 15 || ownerSindikat != targetStrings.get(s)) {
                result.put(String.valueOf(targetStrings.get(s)), s);
            }
        });

        return ResponseEntity.ok(result);
    }
}
