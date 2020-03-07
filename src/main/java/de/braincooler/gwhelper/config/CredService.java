package de.braincooler.gwhelper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CredService {

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
}
