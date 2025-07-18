package com.store.mgmt.inventory.model.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "purchase_orders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate; // Nullable until delivered

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PurchaseOrderStatus status = PurchaseOrderStatus.PENDING;

    @Column(name = "total_estimated_amount", precision = 10, scale = 2)
    private BigDecimal totalEstimatedAmount;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable if the system can generate POs without a specific user
    private User user; // Who created/managed the PO

    // One-to-Many relationship with PurchaseOrderItem
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PurchaseOrderItem> purchaseOrderItems;

    public enum PurchaseOrderStatus {
        PENDING(
            "pending"
        ),
        ORDERED(
            "ordered"
        ),
        RECEIVED_PARTIAL(
            "received_partial"
        ),
        RECEIVED_COMPLETE(
            "received_complete"),
        CANCELLED(
            "cancelled"
        );
        private final String value;
        PurchaseOrderStatus(String value) {
            this.value = value;
        }
        @JsonValue
public String getValue() {
            return value;
        }


    }
}