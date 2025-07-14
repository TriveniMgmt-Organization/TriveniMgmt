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

    List<InventoryItem> findByProductId(UUID productId);

    List<InventoryItem> findByLocationId(UUID locationId);
    List<InventoryItem> findByProductIdAndLocationId(UUID productId, UUID locationId);

    // For optimistic locking when updating stock
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT) // Ensures version is incremented
    Optional<InventoryItem> findById(@NonNull UUID id);

    // Custom query to sum quantities for a product across all locations
    @Query("SELECT SUM(ii.quantity) FROM InventoryItem ii WHERE ii.product.id = :productId")
    Optional<Integer> getTotalQuantityByProductId(@Param("productId") UUID productId);

    // Custom query to sum quantities for a product at a specific location
    @Query("SELECT SUM(ii.quantity) FROM InventoryItem ii WHERE ii.product.id = :productId AND ii.location.id = :locationId")
    Optional<Integer> getTotalQuantityByProductIdAndLocationId(@Param("productId") UUID productId, @Param("locationId") UUID locationId);

    // Find specific inventory item by its unique constraint components
    Optional<InventoryItem> findByProductIdAndLocationIdAndBatchNumberAndExpirationDate(
            UUID productId, UUID locationId, String batchNumber, LocalDateTime expirationDate);
}