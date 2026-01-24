package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

/**
 * StockTransfer entity - Stock transfers between stores.
 * Uses UUID references for Stores to avoid cross-module entity dependencies.
 */
@Entity
@Table(name = "stock_transfers")
@Data
@EqualsAndHashCode(callSuper = false)
public class StockTransfer extends BaseEntity {

    @Column(name = "from_store_id", nullable = false)
    private UUID fromStoreId;

    @Column(name = "to_store_id", nullable = false)
    private UUID toStoreId;

    @ManyToOne
    @JoinColumn(name = "product_template_id", nullable = false)
    private ProductTemplate productTemplate;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StockStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductVariant variant;
}