package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, UUID> {
    
    // Find stock level by inventory item
    Optional<StockLevel> findByInventoryItemId(UUID inventoryItemId);
    
    // Find low stock items (below threshold)
    @Query("SELECT sl FROM StockLevel sl WHERE sl.available < sl.lowStockThreshold AND sl.inventoryItem.deletedAt IS NULL")
    List<StockLevel> findLowStockItems();
    
    // Find low stock items for a specific location
    @Query("SELECT sl FROM StockLevel sl WHERE sl.inventoryItem.location.id = :locationId AND sl.available < sl.lowStockThreshold AND sl.inventoryItem.deletedAt IS NULL")
    List<StockLevel> findLowStockItemsByLocationId(@Param("locationId") UUID locationId);
    
    // Find stock levels for a variant across all locations
    @Query("SELECT sl FROM StockLevel sl WHERE sl.inventoryItem.variant.id = :variantId AND sl.inventoryItem.deletedAt IS NULL")
    List<StockLevel> findByVariantId(@Param("variantId") UUID variantId);
    
    // Get total on-hand quantity for a variant across all locations
    @Query("SELECT COALESCE(SUM(sl.onHand), 0) FROM StockLevel sl WHERE sl.inventoryItem.variant.id = :variantId AND sl.inventoryItem.deletedAt IS NULL")
    Integer getTotalOnHandByVariantId(@Param("variantId") UUID variantId);
    
    // Get total available quantity for a variant across all locations
    @Query("SELECT COALESCE(SUM(sl.available), 0) FROM StockLevel sl WHERE sl.inventoryItem.variant.id = :variantId AND sl.inventoryItem.deletedAt IS NULL")
    Integer getTotalAvailableByVariantId(@Param("variantId") UUID variantId);
}

