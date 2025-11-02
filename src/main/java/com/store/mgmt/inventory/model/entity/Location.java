package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.enums.LocationType;
import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Location extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name; // e.g., "Main Store Floor", "Backroom Storage", "Store #123"

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // If it's a separate store branch

    @Enumerated(EnumType.STRING) // Store enum name as string in DB
    @Column(name = "type", nullable = false, length = 50)
    private LocationType type = LocationType.STORE;

    // One-to-Many relationships:
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<InventoryItem> inventoryItems;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DamageLoss> damageLosses;
}