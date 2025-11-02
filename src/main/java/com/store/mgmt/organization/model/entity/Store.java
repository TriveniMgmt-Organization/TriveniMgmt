package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "stores")
@Data
@EqualsAndHashCode(callSuper = false)
public class Store extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    @ToString.Exclude
    private Organization organization;

    @Column(nullable = false)
    private String name;

    private String location;

    @Column
    private String countryCode;

    private String contactInfo;

    @Enumerated(EnumType.STRING)
    private StoreStatus status = StoreStatus.ACTIVE;

}