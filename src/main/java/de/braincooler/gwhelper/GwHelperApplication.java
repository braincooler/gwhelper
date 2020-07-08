package de.braincooler.gwhelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GwHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(GwHelperApplication.class, args);
        System.out.println("copy");
    }
}
