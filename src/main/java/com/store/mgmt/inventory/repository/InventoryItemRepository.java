package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    // Find by variant (replaces productTemplate)
    List<InventoryItem> findByVariantId(UUID variantId);

    // Find by location
    List<InventoryItem> findByLocationId(UUID locationId);
    
    // Find by variant and location
    List<InventoryItem> findByVariantIdAndLocationId(UUID variantId, UUID locationId);

    // Find by variant, location, and batch lot (unique constraint)
    @Query("SELECT i FROM InventoryItem i WHERE i.variant.id = :variantId AND i.location.id = :locationId AND (:batchLotId IS NULL AND i.batchLot IS NULL OR i.batchLot.id = :batchLotId)")
    Optional<InventoryItem> findByVariantIdAndLocationIdAndBatchLotId(
            @Param("variantId") UUID variantId, 
            @Param("locationId") UUID locationId, 
            @Param("batchLotId") UUID batchLotId);

    // For optimistic locking when updating
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<InventoryItem> findById(@NonNull UUID id);

    // Find items for a template (via variants)
    @Query("SELECT i FROM InventoryItem i WHERE i.variant.template.id = :templateId AND i.deletedAt IS NULL")
    List<InventoryItem> findByTemplateId(@Param("templateId") UUID templateId);

    // Find by location and store (if needed for security)
    @Query("SELECT i FROM InventoryItem i WHERE i.location.id = :locationId AND i.location.store.id = :storeId AND i.deletedAt IS NULL")
    List<InventoryItem> findByLocationIdAndStoreId(@Param("locationId") UUID locationId, @Param("storeId") UUID storeId);

    // Find by variant and store (if needed for security)
    @Query("SELECT i FROM InventoryItem i WHERE i.variant.id = :variantId AND i.location.store.id = :storeId AND i.deletedAt IS NULL")
    List<InventoryItem> findByVariantIdAndStoreId(@Param("variantId") UUID variantId, @Param("storeId") UUID storeId);

    // Find by batch lot
    @Query("SELECT i FROM InventoryItem i WHERE i.batchLot.id = :batchLotId AND i.deletedAt IS NULL")
    List<InventoryItem> findByBatchLotId(@Param("batchLotId") UUID batchLotId);

    // Find items with expiry dates approaching
    @Query("SELECT i FROM InventoryItem i WHERE i.expiryDate BETWEEN :startDate AND :endDate AND i.deletedAt IS NULL ORDER BY i.expiryDate ASC")
    List<InventoryItem> findExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
