package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

/**
 * Role entity - JPA entity for role data with permissions relationship.
 */
@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "permissions")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "permissions")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
