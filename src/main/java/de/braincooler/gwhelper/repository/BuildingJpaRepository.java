package de.braincooler.gwhelper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingJpaRepository extends JpaRepository<BuildingEntity, Integer> {
    List<BuildingEntity> findByUpdateTimestampGreaterThan(Long timestamp);
}
