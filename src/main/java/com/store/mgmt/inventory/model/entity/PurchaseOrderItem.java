package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"purchase_order_id", "product_template_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_template_id", nullable = false)
    private ProductTemplate productTemplate;

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity = 0;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

}