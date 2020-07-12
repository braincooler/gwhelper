package de.braincooler.gwhelper.controller;

import de.braincooler.gwhelper.service.AdvertisementService;
import de.braincooler.gwhelper.service.SiteBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class GwController {
    private final AdvertisementService advertisementService;

    private final SiteBuilder siteBuilder;

    public GwController(AdvertisementService advertisementService, SiteBuilder siteBuilder) {
        this.advertisementService = advertisementService;
        this.siteBuilder = siteBuilder;
    }

    @GetMapping
    public ResponseEntity<?> getAdvertisementList() {
        return ResponseEntity.ok(siteBuilder.buildSite());
    }

    @GetMapping(path = "test")
    public ResponseEntity<?> getMapSize() {
        return ResponseEntity.ok(advertisementService.getMapSize());
    }
}