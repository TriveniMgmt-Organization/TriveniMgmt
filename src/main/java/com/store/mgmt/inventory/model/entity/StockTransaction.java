package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.enums.TransactionType;
import com.store.mgmt.inventory.model.enums.AdjustmentReason;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_transactions",
       indexes = {
           @Index(name = "idx_tx_inventory", columnList = "inventory_item_id"),
           @Index(name = "idx_tx_type", columnList = "type"),
           @Index(name = "idx_tx_timestamp", columnList = "timestamp DESC"),
           @Index(name = "idx_tx_reference", columnList = "reference_type, reference_id")
       })
@Data
@EqualsAndHashCode(callSuper = false)
public class StockTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType type;

    @Column(name = "quantity_delta", nullable = false)
    private int quantityDelta;

    // --- Reference (e.g., PO-123, SALE-456) ---
    @Column(name = "reference_type", length = 50)
    private String referenceType; // "PURCHASE_ORDER", "SALE", "TRANSFER"

    @Column(name = "reference_id")
    private UUID referenceId;

    // --- For transfers ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private InventoryLocation fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private InventoryLocation toLocation;

    // --- For adjustments ---
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 50)
    private AdjustmentReason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}