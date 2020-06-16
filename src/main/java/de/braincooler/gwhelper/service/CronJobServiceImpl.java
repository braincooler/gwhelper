package de.braincooler.gwhelper.service;

import de.braincooler.gwhelper.consumer.GwConsumer;
import de.braincooler.gwhelper.repository.DataRepository;
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
    private final DataRepository dataRepository;

    public CronJobServiceImpl(GwConsumer gwConsumer, DataRepository dataRepository) {
        this.gwConsumer = gwConsumer;
        this.dataRepository = dataRepository;
    }

    @Override
    @PostConstruct
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000)
    public void initSyndWarsAndControlledSektors() {
        dataRepository.getSupportedSyndIds().forEach(syndid -> {
            gwConsumer.initSyndWarsList(syndid);
            gwConsumer.initControlledSektors(syndid);
        });
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
