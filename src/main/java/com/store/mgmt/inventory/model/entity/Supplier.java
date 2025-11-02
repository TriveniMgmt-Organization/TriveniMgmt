package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Supplier extends BaseEntity {
        @ManyToOne
        @JoinColumn(name = "organization_id", nullable = false)
        private Organization organization;

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
        @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Set<PurchaseOrder> purchaseOrders;
    }