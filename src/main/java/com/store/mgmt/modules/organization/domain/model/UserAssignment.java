package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * UserAssignment entity - Tracks user assignments to organizations/stores.
 * Uses UUID references to avoid cross-module entity dependencies.
 */
@Entity
@Table(name = "user_assignments")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserAssignment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @PrePersist
    @PreUpdate
    private void validateAssignment() {
        if (organization == null && store == null) {
            throw new IllegalStateException("Either organization or store must be set for UserAssignment.");
        }
    }
}
