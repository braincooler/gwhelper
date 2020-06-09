package de.braincooler.gwhelper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CredService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredService.class);

    @Value("${gw.user}")
    private String gwUser;

    @Value("${gw.password}")
    private String gwPassword;

    public String getGwUser() {
        return gwUser;
    }

    public String getGwPassword() {
        return gwPassword;
    }

    @PostConstruct
    private void init() {
        LOGGER.info("user '{}'", gwUser);
    }
}
