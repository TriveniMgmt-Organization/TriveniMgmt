package com.store.mgmt.pos.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.entity.ProductTemplate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "pos_sale_items")
@Data
@EqualsAndHashCode(callSuper = true)
public class PosSaleItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private PosSale sale;

    @ManyToOne
    @JoinColumn(name = "product_template_id", nullable = false)
    private ProductTemplate productTemplate;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal;
}