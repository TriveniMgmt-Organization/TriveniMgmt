package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.BatchLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchLotRepository extends JpaRepository<BatchLot, UUID> {
    
    // Find by batch number (should be unique)
    Optional<BatchLot> findByBatchNumber(String batchNumber);
    
    // Find batch lots expiring soon
    @Query("SELECT bl FROM BatchLot bl WHERE bl.expiryDate BETWEEN :startDate AND :endDate AND bl.deletedAt IS NULL ORDER BY bl.expiryDate ASC")
    List<BatchLot> findExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Find expired batch lots
    @Query("SELECT bl FROM BatchLot bl WHERE bl.expiryDate < :currentDate AND bl.deletedAt IS NULL")
    List<BatchLot> findExpired(@Param("currentDate") LocalDate currentDate);
    
    // Find batch lots by supplier
    @Query("SELECT bl FROM BatchLot bl WHERE bl.supplier.id = :supplierId AND bl.deletedAt IS NULL")
    List<BatchLot> findBySupplierId(@Param("supplierId") UUID supplierId);
}

