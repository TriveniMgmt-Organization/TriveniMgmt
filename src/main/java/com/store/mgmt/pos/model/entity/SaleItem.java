package com.store.mgmt.pos.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
public class SaleItem extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal;
}