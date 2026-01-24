package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SaleItem entity - Items within a sale.
 * Uses UUID reference for Store to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "sale_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sale_id", "product_template_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SaleItem extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_template_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductTemplate productTemplate;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Price at the time of sale (historical)

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Example: SaleItem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductVariant variant;
}