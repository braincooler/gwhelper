package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.Building;

public class BuildingMapper {
    private static Building toDto(BuildingEntity buildingEntity) {
        Building building = new Building();
        building.setArea(buildingEntity.getArea());
        building.setControlSynd(buildingEntity.getControlSyndId());
        building.setDescription(buildingEntity.getDescription());
        building.setId(buildingEntity.getId());
        building.setOwnerSynd(buildingEntity.getOwnerSyndId());
        building.setSektorUrl(buildingEntity.getSektorUrl());
        building.setControlSynd(buildingEntity.getControlSyndId());
        building.setStaticControlsyndId(buildingEntity.getStaticControlSyndId());

        return building;
    }

    private static BuildingEntity toEntity(Building building) {

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
