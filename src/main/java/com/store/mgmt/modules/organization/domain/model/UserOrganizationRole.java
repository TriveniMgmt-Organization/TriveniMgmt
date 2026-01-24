package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

/**
 * UserOrganizationRole entity - Links users to organizations with specific roles.
 * Uses UUID references to avoid cross-module entity dependencies.
 */
@Entity
@Table(name = "user_organization_roles", indexes = {
        @Index(name = "idx_uor_user", columnList = "user_id"),
        @Index(name = "idx_uor_organization", columnList = "organization_id"),
        @Index(name = "idx_uor_store", columnList = "store_id"),
        @Index(name = "idx_uor_role", columnList = "role_id"),
        @Index(name = "idx_uor_user_org", columnList = "user_id, organization_id")
})
@Data
@ToString(exclude = { "organization", "store" })
@EqualsAndHashCode(callSuper = true, exclude = { "organization", "store" })
public class UserOrganizationRole extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @PrePersist
    @PreUpdate
    private void validateAssignment() {
        if (organization == null && store == null) {
            throw new IllegalStateException("Either organization or store must be set for UserOrganizationRole.");
        }
    }
}
