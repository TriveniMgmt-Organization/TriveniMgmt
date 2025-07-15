package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import lombok.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "suppliers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends BaseEntity {

        @Column(name = "name", unique = true, nullable = false, length = 255)
        private String name;

        @Column(name = "contact_person", length = 255)
        private String contactPerson;

        @Column(name = "email", length = 255)
        private String email;

        @Column(name = "phone", length = 50)
        private String phone;

        @Column(name = "address", columnDefinition = "TEXT")
        private String address;

        @Column(name = "account_number", length = 100)
        private String accountNumber; // Supplier's account number with the store

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        // One-to-Many relationship with PurchaseOrder
        @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Set<PurchaseOrder> purchaseOrders;
    }