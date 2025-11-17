package com.store.mgmt.inventory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "stock_levels",
       indexes = {
           @Index(name = "idx_stock_available", columnList = "available"),
           @Index(name = "idx_stock_low", columnList = "low_stock_threshold"),
           @Index(name = "idx_stock_inventory_item", columnList = "inventory_item_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "inventoryItem")
@ToString(exclude = "inventoryItem")
public class StockLevel extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    @JsonIgnore
    private InventoryItem inventoryItem;

    @Column(name = "on_hand", nullable = false)
    private int onHand = 0;

    @Column(name = "committed", nullable = false)
    private int committed = 0;

    @Column(name = "available", nullable = false)
    private int available = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 10;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    // --- Update available on change ---
    @PreUpdate
    @PrePersist
    private void updateAvailable() {
        this.available = onHand - committed;
    }

    // --- Helper methods ---
    public void addOnHand(int qty) {
        this.onHand += qty;
    }

    public void addCommitted(int qty) {
        this.committed += qty;
    }
}