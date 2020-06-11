package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.consumer.GwConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class CronJobServiceImpl implements CronJobService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CronJobService.class);

    private final GwConsumer gwConsumer;

    public CronJobServiceImpl(GwConsumer gwConsumer) {
        this.gwConsumer = gwConsumer;
    }

    @Override
    @PostConstruct
    public void initSyndWars() {
        gwConsumer.initSyndWarsList(1635);
        gwConsumer.initControlledSektors(1635);
    }

    @Override
   // @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000)
    public void initControlledSektors() {
        // gwConsumer.initControlledSektors(1635);
    }

    @Override
    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10000)
    public void initBuildingsReadyForAttack() {
        LOGGER.info("<<< --- start --- >>>");
        LocalDateTime timerStart = LocalDateTime.now();
        for (int i = 47; i <= 53; i++) {
            for (int j = 47; j <= 53; j++) {
                gwConsumer.initBuildingsFromSektorPage(i, j, "plants");
                gwConsumer.initBuildingsFromSektorPage(i, j, "tech");
            }
        }
        LOGGER.info("<<< --- end [{} - {}}] --- >>>", timerStart, LocalDateTime.now());
    }
}
