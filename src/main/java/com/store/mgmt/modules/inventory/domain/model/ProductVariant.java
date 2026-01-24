package com.store.mgmt.modules.inventory.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ProductVariant entity - Product variants within an organization.
 * Uses UUID reference for Organization to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "product_variants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "sku"}),
    @UniqueConstraint(columnNames = {"organization_id", "barcode"})
})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"template", "inventoryItems"})
@ToString(exclude = {"template", "inventoryItems"})
public class ProductVariant extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ProductTemplate template;

    @Column(nullable = false)
    private String sku; // e.g., TSHIRT-RED-L

    private String barcode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    // Variant-specific values
    @ElementCollection
    @CollectionTable(name = "variant_attribute_values", joinColumns = @JoinColumn(name = "variant_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributeValues = new HashMap<>(); // e.g., "color" -> "Red"

    private boolean isActive = true;

    @OneToMany(mappedBy = "variant", cascade = {}, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<InventoryItem> inventoryItems;
}