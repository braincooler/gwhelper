package de.braincooler.gwhelper.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

    private final GwService gwService;

    public ScheduledService(GwService gwService) {
        this.gwService = gwService;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    private void initAdvertisementRepo() {
        // G
        for (int i = 47; i <= 53; i++) {
            for (int j = 47; j <= 53; j++) {
                gwService.initSektorOnSale(i, j);
            }
        }

        // P
        gwService.initSektorOnSale(125, 75);
        gwService.initSektorOnSale(121, 79);
        for (int i = 122; i <= 125; i++) {
            for (int j = 76; j <= 78; j++) {
                if (i != 125 && j != 78) {
                    gwService.initSektorOnSale(i, j);
                }
            }
        }

        // Z
        gwService.initSektorOnSale(152, 148);
        for (int i = 149; i <= 152; i++) {
            for (int j = 149; j <= 152; j++) {
                if ((i != 152 && j != 150) && (i != 150 && j != 152)) {
                    gwService.initSektorOnSale(i, j);
                }
            }
        }
    }
}
