package com.store.mgmt.users.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {
//    @ManyToOne
//    @JoinColumn(name = "organization_id", nullable = false)
//    private Organization organization;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}