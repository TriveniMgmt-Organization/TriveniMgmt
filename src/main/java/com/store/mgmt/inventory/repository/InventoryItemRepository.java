package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findByProductTemplateId(UUID productTemplateId);

    List<InventoryItem> findByLocationId(UUID locationId);
    List<InventoryItem> findByProductTemplateIdAndLocationId(UUID productTemplateId, UUID locationId);

    // For optimistic locking when updating stock
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT) // Ensures version is incremented
    Optional<InventoryItem> findById(@NonNull UUID id);

    // Custom query to sum quantities for a product across all locations
    @Query("SELECT SUM(ii.quantity) FROM InventoryItem ii WHERE ii.productTemplate.id = :productTemplateId")
    Optional<Integer> getTotalQuantityByProductTemplateId(@Param("productTemplateId") UUID productTemplateId);

    // Custom query to sum quantities for a product at a specific location
    @Query("SELECT SUM(ii.quantity) FROM InventoryItem ii WHERE ii.productTemplate.id = :productTemplateId AND ii.location.id = :locationId")
    Optional<Integer> getTotalQuantityByProductTemplateIdAndLocationId(@Param("productTemplateId") UUID productTemplateId, @Param("locationId") UUID locationId);

    @Query("SELECT i FROM InventoryItem i WHERE i.store.id = :storeId AND i.store.organization.id = :orgId")
    List<InventoryItem> findByStoreIdAndOrganizationId(@Param("storeId") UUID storeId, @Param("organizationId") UUID orgId);

    Optional<InventoryItem> findByIdAndStoreId(UUID id, UUID storeId);
    Optional<InventoryItem> findByProductTemplateIdAndStoreIdAndBatchNumberAndExpirationDate(
            UUID productTemplateId, UUID storeId, String batchNumber, LocalDateTime expirationDate);
    List<InventoryItem> findByProductTemplateIdAndStoreId(UUID productTemplateId, UUID storeId);
    List<InventoryItem> findByLocationIdAndStoreId( UUID locationId, UUID storeId );
    List<InventoryItem> findByProductTemplateIdAndStoreIdAndLocationId(
            UUID productTemplateId, UUID storeId, UUID locationId);
    Optional<Integer> getTotalQuantityByProductTemplateIdAndStoreId(
            UUID productTemplateId, UUID storeId);
    Optional<Integer> getTotalQuantityByProductTemplateIdAndStoreIdAndLocationId(
            UUID productTemplateId, UUID storeId, UUID locationId);
}