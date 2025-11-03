package com.store.mgmt.inventory.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.store.mgmt.common.model.BaseEntity;

@Entity
@Table(name = "inventory_items",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uq_inventory_item",
               columnNames = {"variant_id", "location_id", "batch_lot_id"}
           )
       },
       indexes = {
           @Index(name = "idx_inventory_variant_location", columnList = "variant_id, location_id"),
           @Index(name = "idx_inventory_batch", columnList = "batch_lot_id"),
           @Index(name = "idx_inventory_expiry", columnList = "expiry_date")
       })
@Data
@EqualsAndHashCode(callSuper = false, exclude = "stockLevel")
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private InventoryLocation location;

    // Nullable — not all items have batches
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_lot_id")
    private BatchLot batchLot;

    // Expiry comes from batch OR override
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // --- Stock Level (Current Quantity) ---
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "stock_level_id", unique = true)
    private StockLevel stockLevel;

    // --- Helper: Auto-create StockLevel on persist ---
    @PrePersist
    private void ensureStockLevel() {
        if (stockLevel == null) {
            stockLevel = new StockLevel();
            stockLevel.setInventoryItem(this);
            stockLevel.setOnHand(0);
            stockLevel.setCommitted(0);
            stockLevel.setAvailable(0);
        }
    }
}