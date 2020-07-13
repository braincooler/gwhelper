package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.model.Sektor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class LocalRepository {
    private final Map<String, Sektor> sektorMap = new HashMap<>();

    public void addSektor(Sektor sektor) {
        sektorMap.put(sektor.getName(), sektor);
    }


    public Sektor getSektor(String sektorName) {
        return sektorMap.get(sektorName);
    }
}
