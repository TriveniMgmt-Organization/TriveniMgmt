package com.store.mgmt.inventory.repository;

// SaleRepository.java

import com.store.mgmt.inventory.model.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
   List<Sale> findBySaleTimestampBetween( LocalDateTime startDate, LocalDateTime endDate );
}