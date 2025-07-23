package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

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
    @ToString.Exclude
    private Set<Store> stores;

    @OneToMany(mappedBy = "organization" )
    @ToString.Exclude
    private Set<UserOrganizationRole> userRoles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Crucial: check if it's a proxy. If it's a proxy and not initialized, getId() might load it.
        // Better to check for null ID for transient entities.
        if (o == null || getClass() != o.getClass()) { // For proxies, getClass() might return proxy class
            // Check if it's a Hibernate proxy and compare based on its superclass
            if (o instanceof HibernateProxy) {
                return equals(((HibernateProxy) o).getHibernateLazyInitializer().getImplementation());
            }
            return false;
        }

        Organization that = (Organization) o;
        // For persisted entities, compare by ID. For transient entities, IDs are null.
        // It's generally safer to consider entities with null IDs as not equal.
        // If IDs are auto-generated, they will be null before persist.
        if (getId() == null || that.getId() == null) {
            return false; // Or handle based on your specific transient entity equality needs
        }
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        // Only use the ID for hashCode. Avoid calling getters on lazy fields.
        // For new (transient) entities, getId() might return null.
        // A constant or 0 is often used for transient entities.
        return getId() != null ? getId().hashCode() : 0; // Use a constant for transient entities
    }

}