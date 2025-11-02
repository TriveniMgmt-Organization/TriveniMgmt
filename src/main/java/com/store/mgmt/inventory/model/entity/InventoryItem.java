package com.store.mgmt.inventory.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.store.mgmt.common.model.BaseEntity;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"variant_id", "location_id", "batch_lot_id"})
})
@Data
@EqualsAndHashCode(callSuper = false)
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private BatchLot batchLot;

    private LocalDate expiryDate;

    @OneToOne(mappedBy = "inventoryItem", cascade = CascadeType.ALL)
    private StockLevel stockLevel;
}