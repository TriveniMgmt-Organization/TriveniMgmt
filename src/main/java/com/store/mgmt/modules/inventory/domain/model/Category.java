package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;

import java.util.UUID;

/**
 * Category entity - Product categories within an organization.
 * Uses UUID reference for Organization to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "code"}),
    @UniqueConstraint(columnNames = {"organization_id", "name"})
})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Data
@EqualsAndHashCode(callSuper = false)
public class Category extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;
    private String description;
    private boolean isActive = true;
}