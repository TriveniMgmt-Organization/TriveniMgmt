package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class Product extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String sku; // Stock Keeping Unit - Unique identifier for the product (e.g., UPC/EAN) - UNIQUE

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "retail_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @Column(name = "cost_price",nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    @Column(nullable = false)
    private boolean isActive = true;// Is the product currently being sold?

    @Column(name = "category_id")
    private UUID categoryId;

    @ManyToOne(fetch = FetchType.LAZY) // Many products to one category
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Column(name = "unit_of_measure_id")
    private UUID unitOfMeasureId; // Foreign key to UnitOfMeasure entity, if applicable

    @ManyToOne(fetch = FetchType.LAZY) // Many products to one unit of measure
    @JoinColumn(name = "unit_of_measure_id", nullable = false) // Foreign key column
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "brand", length = 100)
    private String brand;    private Integer reorderPoint; // Minimum stock level to trigger a reorder

    @Column(name = "requires_expiration_date", nullable = false)
    private boolean requiresExpirationDate = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // Exclude from toString to prevent StackOverflowError
    @EqualsAndHashCode.Exclude // Exclude from equals/hashCode
    private Set<InventoryItem> inventoryItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<SaleItem> saleItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PurchaseOrderItem> purchaseOrderItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DamageLoss> damageLosses;

    // Optional: If a discount can apply specifically to one product
    // @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    // @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    // private Set<Discount> discounts;
}