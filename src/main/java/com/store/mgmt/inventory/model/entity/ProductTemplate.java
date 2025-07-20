package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "product_templates")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductTemplate extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, unique = true)
    private String sku; // Stock Keeping Unit - Unique identifier for the product (e.g., UPC/EAN) - UNIQUE

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    @Column(nullable = false)
    private boolean isActive = true;// Is the product currently being sold?

    @ManyToOne(fetch = FetchType.LAZY) // Many products to one category
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY) // Many products to one unit of measure
    @JoinColumn(name = "unit_of_measure_id", nullable = false, insertable = false, updatable = false) // Foreign key column
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "brand", length = 100)
    private String brand;

    private Integer reorderPoint; // Minimum stock level to trigger a reorder

    @Column(name = "requires_expiration_date", nullable = false)
    private boolean requiresExpirationDate = false;

    @OneToMany(mappedBy = "productTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // Exclude from toString to prevent StackOverflowError
    @EqualsAndHashCode.Exclude // Exclude from equals/hashCode
    private Set<InventoryItem> inventoryItems;

    @OneToMany(mappedBy = "productTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SaleItem> saleItems;

    @OneToMany(mappedBy = "productTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PurchaseOrderItem> purchaseOrderItems;

    @OneToMany(mappedBy = "productTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DamageLoss> damageLosses;

    // Optional: If a discount can apply specifically to one product
    // @OneToMany(mappedBy = "productTemplate", fetch = FetchType.LAZY)
    // @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    // private Set<Discount> discounts;
}