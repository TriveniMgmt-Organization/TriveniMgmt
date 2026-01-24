package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Filter;

import java.util.Set;
import java.util.UUID;

/**
 * Supplier entity - Suppliers within an organization.
 * Uses UUID reference for Organization to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "suppliers")
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Supplier extends BaseEntity {

        @Column(name = "organization_id", nullable = false)
        private UUID organizationId;

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

        // One-to-Many relationship with PurchaseOrder
        // Removed CASCADE.ALL to prevent deep object graph traversal when saving related entities
        @OneToMany(mappedBy = "supplier", orphanRemoval = true, fetch = FetchType.LAZY)
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Set<PurchaseOrder> purchaseOrders;
    }