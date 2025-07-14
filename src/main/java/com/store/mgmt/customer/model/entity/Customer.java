package com.store.mgmt.customer.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer extends BaseEntity {
    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column
    private String loyaltyId;

    @Column(nullable = false)
    private boolean isActive = true;
}