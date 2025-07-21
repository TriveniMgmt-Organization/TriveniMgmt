package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.enums.StockStatus;
import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "stock_transfers")
@Data
@EqualsAndHashCode(callSuper = true)
public class StockTransfer extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "from_store_id", nullable = false)
    private Store fromStore;

    @ManyToOne
    @JoinColumn(name = "to_store_id", nullable = false)
    private Store toStore;

    @ManyToOne
    @JoinColumn(name = "product_template_id", nullable = false)
    private ProductTemplate productTemplate;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StockStatus status;
}