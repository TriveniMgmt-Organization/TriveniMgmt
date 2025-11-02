package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.enums.TransactionType;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Data
@EqualsAndHashCode(callSuper = false)
public class StockTransaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // RECEIPT, SALE, TRANSFER_IN, etc.

    @Column(nullable = false)
    private int quantityDelta;

    private String reference; // PO ID, Sale ID, etc.

    @ManyToOne
    private User user;

    private LocalDateTime timestamp = LocalDateTime.now();
}