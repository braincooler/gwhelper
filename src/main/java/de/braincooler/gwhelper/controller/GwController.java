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

    //@GetMapping
    public ResponseEntity<?> getTargets() {
        return ResponseEntity.ok(gwService.getTargetLinks());
    }

    @GetMapping(path = "/atack")
    public ResponseEntity<?> getBuildingsReadyForAttack() {
        return ResponseEntity.ok(gwService.getBuildingsReadyForAttack());
    }

    @GetMapping(path = "/atack/notop")
    public ResponseEntity<?> getBuildingsReadyForAttackNoTop() {
        return ResponseEntity.ok(gwService.getBuildingsReadyForAttackNoTop());
    }

    @GetMapping(path = "/logs")
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(gwService.getLogs());
    }

    //@GetMapping(path = "/time/{buildingId}")
    public ResponseEntity<?> getAttackTime(@PathVariable int buildingId) {
        return ResponseEntity.ok(gwService.getAtackTime(buildingId));
    }

    @GetMapping(path = "/test/{buildingId}")
    public ResponseEntity<?> getTest(@PathVariable int buildingId) {
        return ResponseEntity.ok(gwService.getStaticControlSyndId(buildingId));
    }
}