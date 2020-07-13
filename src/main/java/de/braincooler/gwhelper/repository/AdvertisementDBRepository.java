package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.persistance.AdvertisementEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementDBRepository extends CrudRepository<AdvertisementEntity, Integer> {
    List<AdvertisementEntity> findAllBySektorName(String sektorName);

    List<AdvertisementEntity> findAllByIsActive(boolean active);
}
