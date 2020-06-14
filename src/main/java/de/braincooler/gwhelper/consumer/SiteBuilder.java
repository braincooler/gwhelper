package de.braincooler.gwhelper.consumer;

import de.braincooler.gwhelper.Building;

import java.util.List;

public interface SiteBuilder {
    String buildSite(List<Building> buildings, int syndId);
}
