package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ProductTemplate entity - Product templates within an organization.
 * Uses UUID reference for Organization to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "product_templates")
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ProductTemplate extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    private String imageUrl;

    private Integer reorderPoint;
    private boolean requiresExpiry = false;
    
    @Column(nullable = false)
    private boolean isActive = true;

    // Dynamic attributes (color, size, etc.)
    @ElementCollection
    @CollectionTable(name = "template_attributes", joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_type")
    private Map<String, String> attributes = new HashMap<>(); // e.g., "color" -> "select"

    // Removed CASCADE.ALL to prevent circular dependency when saving InventoryItem
    // InventoryItem → ProductVariant → ProductTemplate → variants → InventoryItems (loop)
    @OneToMany(mappedBy = "template", orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ProductVariant> variants;
}