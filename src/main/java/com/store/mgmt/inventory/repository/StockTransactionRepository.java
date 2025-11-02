package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.StockTransaction;
import com.store.mgmt.inventory.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    
    // Find all transactions for an inventory item (chronological)
    @Query("SELECT t FROM StockTransaction t WHERE t.inventoryItem.id = :inventoryItemId ORDER BY t.timestamp DESC")
    List<StockTransaction> findByInventoryItemIdOrderByTimestampDesc(@Param("inventoryItemId") UUID inventoryItemId);
    
    // Find transactions by type for an inventory item
    @Query("SELECT t FROM StockTransaction t WHERE t.inventoryItem.id = :inventoryItemId AND t.type = :type ORDER BY t.timestamp DESC")
    List<StockTransaction> findByInventoryItemIdAndType(@Param("inventoryItemId") UUID inventoryItemId, @Param("type") TransactionType type);
    
    // Find transactions by date range
    @Query("SELECT t FROM StockTransaction t WHERE t.timestamp BETWEEN :startDate AND :endDate ORDER BY t.timestamp DESC")
    List<StockTransaction> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find transactions by reference (e.g., PO ID, Sale ID)
    List<StockTransaction> findByReference(String reference);
    
    // Calculate sum of quantity deltas for an inventory item (for stock level calculation)
    @Query("SELECT COALESCE(SUM(t.quantityDelta), 0) FROM StockTransaction t WHERE t.inventoryItem.id = :inventoryItemId")
    Integer sumQuantityDeltaByInventoryItemId(@Param("inventoryItemId") UUID inventoryItemId);
    
    // Find transactions for a variant across all inventory items
    @Query("SELECT t FROM StockTransaction t WHERE t.inventoryItem.variant.id = :variantId ORDER BY t.timestamp DESC")
    List<StockTransaction> findByVariantId(@Param("variantId") UUID variantId);
}

