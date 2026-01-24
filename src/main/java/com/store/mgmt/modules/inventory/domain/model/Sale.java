package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Sale entity - Sales within a store.
 * Uses UUID references for Store and User to avoid cross-module entity dependencies.
 */
@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Sale extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "sale_timestamp", nullable = false)
    private LocalDateTime saleTimestamp = LocalDateTime.now();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_id", length = 255)
    private String transactionId; // From payment gateway

    @Column(name = "user_id") // Nullable if guest checkout
    private UUID userId; // Cashier or user who processed the sale

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // One-to-Many relationship with SaleItem
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SaleItem> saleItems;
}
