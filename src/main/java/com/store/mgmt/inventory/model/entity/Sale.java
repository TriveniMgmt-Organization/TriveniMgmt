package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.enums.PaymentMethod;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Sale extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable if guest checkout
    private User user; // Cashier or user who processed the sale

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // One-to-Many relationship with SaleItem
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SaleItem> saleItems;
}
