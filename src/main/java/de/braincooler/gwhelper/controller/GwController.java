package de.braincooler.gwhelper.controller;

import de.braincooler.gwhelper.service.GwService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class GwController {
    private final GwService gwService;

    public GwController(GwService gwService) {
        this.gwService = gwService;
    }

    @GetMapping(path = "/{syndId}")
    public ResponseEntity<?> getTargets(@PathVariable int syndId) {
        return ResponseEntity.ok(gwService.getBuildingsWithoutTurel(syndId));
    }

    @GetMapping(path = "/warlist/{syndId}")
    public ResponseEntity<?> getWarlist(@PathVariable int syndId) {
        return ResponseEntity.ok(gwService.getWarlist(syndId));
    }

    @GetMapping(path = "/init/{sektorX}/{sektorY}")
    public ResponseEntity<?> initSektor(@PathVariable int sektorX, @PathVariable int sektorY) {
        gwService.initSektor(sektorX, sektorY);
        return ResponseEntity.ok("ok");
    }

    @GetMapping(path = "/all")
    public ResponseEntity<?> initSektor() {
        return ResponseEntity.ok(gwService.getAll());
    }

    @GetMapping(path = "/control/{syndId}")
    public ResponseEntity<?> initSektor(@PathVariable int syndId) {
        return ResponseEntity.ok(gwService.getControlledSektors(syndId));
    }

    @GetMapping(path = "/controledBuildings/{syndId}")
    public ResponseEntity<?> getControlledBuildings(@PathVariable int syndId) {
        return ResponseEntity.ok(gwService.getControlledBuildings(syndId));
    }

    @GetMapping
    public ResponseEntity<?> getHome() {
        String linkTemplate = "<a href=\"%s\" target=\"_blank\">%s</a>";
        return ResponseEntity.ok(
                String.format(linkTemplate, "https://gw1635.herokuapp.com/1635", 1635) + "</br>" +
                        String.format(linkTemplate, "https://gw1635.herokuapp.com/1637", 1637)
        );
    }
}