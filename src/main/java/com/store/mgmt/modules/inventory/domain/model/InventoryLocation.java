package com.store.mgmt.modules.inventory.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * InventoryLocation entity - Inventory locations within a store.
 * Uses UUID reference for Store to avoid cross-module entity dependency.
 */
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

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

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
    // No cascade - InventoryItem manages its own lifecycle
    @OneToMany(mappedBy = "location", orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<InventoryItem> inventoryItems = new HashSet<>();

    // No cascade - DamageLoss manages its own lifecycle
    @OneToMany(mappedBy = "location", orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DamageLoss> damageLosses = new HashSet<>();
}