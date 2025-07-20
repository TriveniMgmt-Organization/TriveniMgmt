package com.store.mgmt.users.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "organizationRoles")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

//    @ManyToOne
//    @JoinColumn(name = "organization_id", nullable = false)
//    private Organization organization;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;


    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserOrganizationRole> organizationRoles;
}