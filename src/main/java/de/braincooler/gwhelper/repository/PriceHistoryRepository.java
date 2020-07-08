package de.braincooler.gwhelper.repository;

import de.braincooler.gwhelper.persistance.PriceHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceHistoryRepository extends CrudRepository<PriceHistoryEntity, Long> {
}
