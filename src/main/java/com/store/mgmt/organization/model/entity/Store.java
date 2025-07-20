package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "stores")
@Data
public class Store extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    private String location;

    @Column
    private String countryCode;

    private String contactInfo;

    @Enumerated(EnumType.STRING)
    private StoreStatus status = StoreStatus.ACTIVE;

    public enum StoreStatus {
        ACTIVE, INACTIVE, CLOSED
    }
}