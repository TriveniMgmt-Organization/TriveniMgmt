package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "code"}),
    @UniqueConstraint(columnNames = {"organization_id", "name"})
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Category extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;
    private String description;
    private boolean isActive = true;
}