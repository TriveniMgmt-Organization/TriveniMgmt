package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Permission entity - JPA entity for permission data.
 */
@Entity
@Table(name = "permissions")
@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}
