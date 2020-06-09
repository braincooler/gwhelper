package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.Building;

public class BuildingMapper {
    public static Building toDto(BuildingEntity buildingEntity) {
        Building building = new Building();
        building.setSektorName(buildingEntity.getSektorName());
        building.setArea(buildingEntity.getArea());
        building.setControlSynd(buildingEntity.getControlSyndId());
        building.setDescription(buildingEntity.getDescription());
        building.setId(buildingEntity.getId());
        building.setOwnerSynd(buildingEntity.getOwnerSyndId());
        building.setSektorUrl(buildingEntity.getSektorUrl());
        building.setControlSynd(buildingEntity.getControlSyndId());
        building.setStaticControlsyndId(buildingEntity.getStaticControlSyndId());
        String buildingUrl = "http://www.gwars.ru/object.php?id=" + buildingEntity.getId();
        building.setUrl(buildingUrl);

        return building;
    }

    public static BuildingEntity toEntity(Building building) {

        BuildingEntity buildingEntity = new BuildingEntity(
                building.getId(),
                building.getOwnerSynd(),
                building.getControlSynd(),
                building.getStaticControlsyndId(),
                building.getArea(),
                building.getSektorName(),
                building.getSektorUrl(),
                building.getDescription()
        );

        return buildingEntity;
    }
}
