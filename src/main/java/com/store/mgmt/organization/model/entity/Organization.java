package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
public class Organization extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column()
    private String description;

    @Column(name = "contact_info")
    private String contactInfo;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Store> stores;

    @OneToMany(mappedBy = "organization" )
    private Set<UserOrganizationRole> userRoles;
}