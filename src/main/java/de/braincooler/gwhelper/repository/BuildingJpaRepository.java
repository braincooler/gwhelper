package de.braincooler.gwhelper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingJpaRepository extends JpaRepository<BuildingEntity, Integer> {
}
