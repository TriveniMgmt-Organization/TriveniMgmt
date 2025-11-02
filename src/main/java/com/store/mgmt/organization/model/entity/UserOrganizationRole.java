package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@Entity
@Table(name = "user_organization_roles")
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

    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (!(o instanceof UserOrganizationRole)) return false;
    //     UserOrganizationRole that = (UserOrganizationRole) o;
    //     return Objects.equals(getId(), that.getId());
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(getId());
    // }
}