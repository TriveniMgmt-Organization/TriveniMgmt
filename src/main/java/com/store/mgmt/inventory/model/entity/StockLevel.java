package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "stock_levels")
@Data
@EqualsAndHashCode(callSuper = false)
public class StockLevel extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(nullable = false)
    private int onHand = 0;

    @Column(nullable = false)
    private int committed = 0;

    @Column(nullable = false)
    private int available; // onHand - committed

    private int lowStockThreshold = 10;
    private Integer maxStockLevel;
}