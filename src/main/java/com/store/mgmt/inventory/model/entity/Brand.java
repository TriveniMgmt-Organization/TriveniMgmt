package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "brands")
@Data
@EqualsAndHashCode(callSuper = false)
public class Brand extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column
    private String logoUrl;

    @Column
    private String website;

    @Column(nullable = false)
    private boolean isActive = true;
}