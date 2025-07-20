package com.store.mgmt.customer.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "loyalty_points", nullable = false)
    private int loyaltyPoints = 0;

    @Column(nullable = false)
    private boolean isActive = true;
}