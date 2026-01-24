package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Discount entity - Discounts within an organization/store.
 * Uses UUID references for Organization and Store to avoid cross-module entity dependencies.
 */
@Entity
@Table(name = "discounts")
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Discount extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private DiscountType type;

    @Column(name = "value", nullable = false, precision = 10, scale = 4) // Scale 4 for percentages (e.g., 0.1250)
    private BigDecimal value;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_template_id") // Nullable if store-wide or category specific
    private ProductTemplate productTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // Nullable if store-wide or product specific
    private Category category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "minimum_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minimumPurchaseAmount;

    @Column(name = "minimum_item_quantity")
    private Integer minimumItemQuantity;
}