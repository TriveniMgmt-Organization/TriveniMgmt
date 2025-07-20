package com.store.mgmt.inventory.model.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "discounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Discount extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

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

    public enum DiscountType {
        PERCENTAGE(
            "percentage" // e.g., 10% off
        ),
        FIXED_AMOUNT(
            "fixed_amount" // e.g., $5 off
        ),
        BOGO("bogo"), // Buy One Get One (specific logic handled in service)
        BUNDLE("bundle"); // Bundle discount (specific logic handled in service)

        private final String value;

        DiscountType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }
}