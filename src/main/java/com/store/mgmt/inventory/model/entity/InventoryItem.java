package com.store.mgmt.inventory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
           @Index(name = "idx_inventory_expiry", columnList = "expiry_date"),
           @Index(name = "idx_inventory_store", columnList = "location_id") // For store-level queries
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"variant", "location", "batchLot", "stockLevel"})
@ToString(exclude = {"variant", "location", "batchLot", "stockLevel"})
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
    // StockLevel has its own ID and references back to InventoryItem
    @OneToOne(mappedBy = "inventoryItem", fetch = FetchType.LAZY, cascade = {}, orphanRemoval = true)
    @JsonIgnore
    private StockLevel stockLevel;
}