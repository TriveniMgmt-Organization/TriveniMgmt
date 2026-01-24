package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
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