package com.store.mgmt.modules.inventory.domain.repository;

import com.store.mgmt.modules.inventory.domain.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InventoryItem aggregate.
 * This is a port - implementations are in the infrastructure layer.
 */
public interface InventoryItemRepository {

    /**
     * Find an inventory item by ID.
     */
    Optional<InventoryItem> findById(InventoryItemId id);

    /**
     * Find an inventory item by ID and store ID (tenant-safe).
     */
    Optional<InventoryItem> findByIdAndStoreId(InventoryItemId id, StoreId storeId);

    /**
     * Find all inventory items for a store.
     */
    List<InventoryItem> findByStoreId(StoreId storeId);

    /**
     * Find all inventory items at a specific location.
     */
    List<InventoryItem> findByLocationId(LocationId locationId);

    /**
     * Find inventory item by variant and location (unique combination).
     */
    Optional<InventoryItem> findByVariantIdAndLocationId(ProductVariantId variantId, LocationId locationId);

    /**
     * Find all low stock items for a store.
     */
    List<InventoryItem> findLowStockByStoreId(StoreId storeId);

    /**
     * Find items expiring within the given number of days.
     */
    List<InventoryItem> findExpiringSoon(StoreId storeId, int daysThreshold);

    /**
     * Check if an item exists for the given variant and location.
     */
    boolean existsByVariantIdAndLocationId(ProductVariantId variantId, LocationId locationId);

    /**
     * Save an inventory item.
     */
    InventoryItem save(InventoryItem item);

    /**
     * Delete an inventory item.
     */
    void delete(InventoryItem item);

    /**
     * Delete an inventory item by ID.
     */
    void deleteById(InventoryItemId id);
}
