package de.braincooler.gwhelper.controller;

import de.braincooler.gwhelper.service.GwService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class GwController {

    private final GwService gwService;

    public GwController(GwService gwService) {
        this.gwService = gwService;
    }

    @GetMapping
    public ResponseEntity<?> getTargets() {
        return ResponseEntity.ok(gwService.getTargetLinks());
    }

    @GetMapping(path = "/sektor")
    public ResponseEntity<?> getSektorObject() {
        return ResponseEntity.ok(gwService.getSektorObject());
    }

    @GetMapping(path = "/logs")
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(gwService.getLogs());
    }
}