package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"organization_id", "sku"}),
    @UniqueConstraint(columnNames = {"organization_id", "barcode"})
})
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"template", "organization", "inventoryItems"})
@ToString(exclude = {"template", "organization", "inventoryItems"})
public class ProductVariant extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

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

    @OneToMany(mappedBy = "variant")
    private Set<InventoryItem> inventoryItems;
}