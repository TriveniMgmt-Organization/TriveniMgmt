package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "user_organization_roles", indexes = {
        @Index(name = "idx_uor_user", columnList = "user_id"),
        @Index(name = "idx_uor_organization", columnList = "organization_id"),
        @Index(name = "idx_uor_store", columnList = "store_id"),
        @Index(name = "idx_uor_role", columnList = "role_id"),
        @Index(name = "idx_uor_user_org", columnList = "user_id, organization_id")
})
@Data
@ToString(exclude = {"user", "organization", "role", "store"})
@EqualsAndHashCode(callSuper = true, exclude = {"user", "organization", "role", "store"})
public class UserOrganizationRole extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

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