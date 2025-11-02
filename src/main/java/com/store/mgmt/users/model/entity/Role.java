package com.store.mgmt.users.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "permissions")
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

//    @ManyToOne
//    @JoinColumn(name = "organization_id", nullable = false)
//    private Organization organization;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (!(o instanceof Role)) return false;
    //     Role role = (Role) o;
    //     return Objects.equals(getId(), role.getId()) && Objects.equals(name, role.name);
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(getId(), name);
    // }
}