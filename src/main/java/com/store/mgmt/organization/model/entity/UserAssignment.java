package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_assignments")
@Data
public class UserAssignment extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @PrePersist
    @PreUpdate
    private void validateAssignment() {
        if (organization == null && store == null) {
            throw new IllegalStateException("Either organization or store must be set for UserAssignment.");
        }
    }
}