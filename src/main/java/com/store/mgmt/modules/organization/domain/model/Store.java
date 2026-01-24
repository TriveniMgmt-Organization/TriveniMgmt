package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_store_organization", columnList = "organization_id"),
        @Index(name = "idx_store_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = false, exclude = "organization")
@ToString(exclude = "organization")
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
}
