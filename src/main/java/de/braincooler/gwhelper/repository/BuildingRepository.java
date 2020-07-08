package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.persistance.BuildingEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends CrudRepository<BuildingEntity, Integer> {
    List<BuildingEntity> findAllBySektorName(String sektorName);
}
