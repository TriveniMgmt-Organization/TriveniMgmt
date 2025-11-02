package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "product_templates")
@Data
@EqualsAndHashCode(callSuper = false, exclude = "variants")
public class ProductTemplate extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

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

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ProductVariant> variants;
}