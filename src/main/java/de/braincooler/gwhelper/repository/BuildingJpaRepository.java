package de.braincooler.gwhelper.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingJpaRepository extends CrudRepository<BuildingEntity, Integer> {
    List<BuildingEntity> findByUpdateTimestampGreaterThanAndTargetOfSyndIdIs(Long timestamp, int targetOfSyndId);
}
