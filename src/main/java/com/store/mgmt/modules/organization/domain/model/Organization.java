package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"stores", "userRoles"})
@ToString(exclude = {"stores", "userRoles"})
public class Organization extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column()
    private String description;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "applied_template_code", length = 50)
    private String appliedTemplateCode;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Store> stores;

    @OneToMany(mappedBy = "organization")
    private Set<UserOrganizationRole> userRoles;
}
