package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.store.mgmt.common.model.BaseEntity;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
        // Unique constraint on product_id, location_id, batch_number, expiration_date
        // This ensures a specific batch/exp date of a product at a location is a unique inventory item
        @UniqueConstraint(columnNames = {"product_template_id", "location_id", "batch_number", "expiration_date"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_template_id", nullable = false)
    private ProductTemplate productTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "expiration_date")
    private LocalDate expirationDate; // Nullable, if product doesn't expire

    @Column(name = "retail_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @Column(name = "cost_price",nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "batch_number", length = 100)
    private String batchNumber; // Nullable, for tracking specific batches

    @Column(name = "lot_number", length = 100)
    private String lotNumber; // Nullable, for tracking specific lots

    @Column(name = "last_stock_update", nullable = false)
    private LocalDateTime lastStockUpdate = LocalDateTime.now(); // When the quantity was last changed

    @Column(nullable = false)
    private int lowStockThreshold; // Alert when stock falls below this

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    public enum InventoryStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        ON_ORDER,
        DAMAGED,
        EXPIRED
    }
}