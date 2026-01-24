package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.modules.products.domain.event.ProductVariantCreated;
import com.store.mgmt.modules.products.domain.event.ProductVariantDeactivated;
import com.store.mgmt.modules.products.domain.event.ProductVariantPriceChanged;
import com.store.mgmt.modules.products.domain.exception.InvalidPriceException;
import com.store.mgmt.shared.domain.model.AggregateRoot;
import com.store.mgmt.shared.infrastructure.security.TenantContext;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ProductVariant aggregate root - represents a specific variant of a product template.
 * Contains SKU, barcode, pricing, and variant-specific attributes.
 */
public class ProductVariant extends AggregateRoot<ProductVariantId> {

    private final ProductVariantId id;
    private ProductTemplateId templateId;
    private Sku sku;
    private Barcode barcode;
    private Money costPrice;
    private Money retailPrice;
    private ProductAttributes attributeValues;
    private boolean active;
    private OrganizationId organizationId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private ProductVariant(ProductVariantId id) {
        this.id = id;
        this.active = true;
        this.attributeValues = ProductAttributes.empty();
    }

    @Override
    public ProductVariantId getId() {
        return id;
    }

    /**
     * Factory method to create a new product variant.
     */
    public static ProductVariant create(
            ProductTemplateId templateId,
            Sku sku,
            Barcode barcode,
            Money costPrice,
            Money retailPrice,
            ProductAttributes attributeValues
    ) {
        Objects.requireNonNull(templateId, "Template ID is required");
        Objects.requireNonNull(sku, "SKU is required");
        Objects.requireNonNull(costPrice, "Cost price is required");
        Objects.requireNonNull(retailPrice, "Retail price is required");

        // Business rule: retail price should be >= cost price
        if (retailPrice.isLessThan(costPrice)) {
            throw InvalidPriceException.retailPriceBelowCost();
        }

        TenantContext tenant = TenantContext.current();

        ProductVariant variant = new ProductVariant(ProductVariantId.generate());
        variant.templateId = templateId;
        variant.sku = sku;
        variant.barcode = barcode;
        variant.costPrice = costPrice;
        variant.retailPrice = retailPrice;
        variant.attributeValues = attributeValues != null ? attributeValues : ProductAttributes.empty();
        variant.organizationId = OrganizationId.of(tenant.organizationId());
        variant.createdAt = LocalDateTime.now();
        variant.updatedAt = variant.createdAt;

        variant.registerEvent(new ProductVariantCreated(
                variant.id,
                variant.templateId,
                variant.sku,
                variant.retailPrice
        ));

        return variant;
    }

    /**
     * Reconstitute from persistence.
     */
    public static ProductVariant reconstitute(
            ProductVariantId id,
            ProductTemplateId templateId,
            Sku sku,
            Barcode barcode,
            Money costPrice,
            Money retailPrice,
            ProductAttributes attributeValues,
            boolean active,
            OrganizationId organizationId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        ProductVariant variant = new ProductVariant(id);
        variant.templateId = templateId;
        variant.sku = sku;
        variant.barcode = barcode;
        variant.costPrice = costPrice;
        variant.retailPrice = retailPrice;
        variant.attributeValues = attributeValues != null ? attributeValues : ProductAttributes.empty();
        variant.active = active;
        variant.organizationId = organizationId;
        variant.createdAt = createdAt;
        variant.updatedAt = updatedAt;
        variant.deletedAt = deletedAt;
        return variant;
    }

    // ==================== Commands ====================

    public void updatePrices(Money newCostPrice, Money newRetailPrice) {
        Objects.requireNonNull(newCostPrice, "Cost price is required");
        Objects.requireNonNull(newRetailPrice, "Retail price is required");

        if (newRetailPrice.isLessThan(newCostPrice)) {
            throw InvalidPriceException.retailPriceBelowCost();
        }

        boolean pricesChanged = !newCostPrice.equals(this.costPrice) || !newRetailPrice.equals(this.retailPrice);

        if (pricesChanged) {
            Money oldCostPrice = this.costPrice;
            Money oldRetailPrice = this.retailPrice;

            this.costPrice = newCostPrice;
            this.retailPrice = newRetailPrice;
            this.updatedAt = LocalDateTime.now();

            registerEvent(new ProductVariantPriceChanged(
                    id,
                    oldCostPrice,
                    newCostPrice,
                    oldRetailPrice,
                    newRetailPrice
            ));
        }
    }

    public void updateSku(Sku newSku) {
        Objects.requireNonNull(newSku, "SKU is required");
        this.sku = newSku;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBarcode(Barcode newBarcode) {
        this.barcode = newBarcode;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAttributeValues(ProductAttributes attributeValues) {
        this.attributeValues = attributeValues != null ? attributeValues : ProductAttributes.empty();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (!this.active) {
            this.active = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void deactivate() {
        if (this.active) {
            this.active = false;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new ProductVariantDeactivated(id));
        }
    }

    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.updatedAt = this.deletedAt;
            this.active = false;
        }
    }

    // ==================== Queries ====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Money calculateMargin() {
        return retailPrice.subtract(costPrice);
    }

    public boolean hasProfitMargin() {
        return retailPrice.isGreaterThan(costPrice);
    }

    // ==================== Getters ====================

    public ProductTemplateId getTemplateId() {
        return templateId;
    }

    public Sku getSku() {
        return sku;
    }

    public Barcode getBarcode() {
        return barcode;
    }

    public Money getCostPrice() {
        return costPrice;
    }

    public Money getRetailPrice() {
        return retailPrice;
    }

    public ProductAttributes getAttributeValues() {
        return attributeValues;
    }

    public boolean isActive() {
        return active;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
