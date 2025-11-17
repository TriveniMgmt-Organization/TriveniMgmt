package com.store.mgmt.inventory.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.inventory.model.enums.InventoryLocationType;
import com.store.mgmt.inventory.model.entity.DamageLoss;
import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "locations", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"store_id", "name"})
       },
       indexes = {
           @Index(name = "idx_location_store", columnList = "store_id"),
           @Index(name = "idx_location_type", columnList = "type")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"inventoryItems", "damageLosses"})
@ToString(exclude = {"inventoryItems", "damageLosses"})
public class InventoryLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", nullable = false, length = 255)
    private String name; // e.g., "Backroom", "Shelf A"

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private InventoryLocationType type = InventoryLocationType.STORE;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // --- Relationships ---
    @OneToMany(mappedBy = "location", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<InventoryItem> inventoryItems = new HashSet<>();

    @OneToMany(mappedBy = "location", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DamageLoss> damageLosses = new HashSet<>();
}