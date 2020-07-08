package de.braincooler.gwhelper.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

    private final AdvertisementService advertisementService;

    public ScheduledService(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Scheduled(fixedDelay = 1 * 60 * 1000)
    private void initAdvertisementRepo() {
        advertisementService.initSektorOnSale(149, 150);
        /*
        // G
        for (int i = 47; i <= 53; i++) {
            for (int j = 47; j <= 53; j++) {
                advertisementService.initSektorOnSale(i, j);
            }
        }

        // P
        advertisementService.initSektorOnSale(125, 75);
        advertisementService.initSektorOnSale(121, 79);
        for (int i = 122; i <= 125; i++) {
            for (int j = 76; j <= 78; j++) {
                if (i != 125 && j != 78) {
                    advertisementService.initSektorOnSale(i, j);
                }
            }
        }

        // Z
        advertisementService.initSektorOnSale(152, 148);
        for (int i = 149; i <= 152; i++) {
            for (int j = 149; j <= 152; j++) {
                if (!(i == 152 && j == 150) && !(i == 150 && j == 152)) {
                    advertisementService.initSektorOnSale(i, j);
                }
            }
        }

 */
    }
}
