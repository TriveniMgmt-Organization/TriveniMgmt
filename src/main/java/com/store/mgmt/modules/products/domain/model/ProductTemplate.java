package com.store.mgmt.modules.products.domain.model;

import com.store.mgmt.modules.products.domain.event.ProductTemplateCreated;
import com.store.mgmt.modules.products.domain.event.ProductTemplateDeleted;
import com.store.mgmt.modules.products.domain.event.ProductTemplateUpdated;
import com.store.mgmt.shared.domain.model.AggregateRoot;
import com.store.mgmt.shared.infrastructure.security.TenantContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ProductTemplate aggregate root - represents a master product definition.
 * Contains all invariants and business logic for product templates.
 */
public class ProductTemplate extends AggregateRoot<ProductTemplateId> {

    private final ProductTemplateId id;
    private String name;
    private String description;
    private CategoryId categoryId;
    private BrandId brandId;
    private UnitOfMeasureId unitOfMeasureId;
    private String imageUrl;
    private Integer reorderPoint;
    private boolean requiresExpiry;
    private boolean active;
    private ProductAttributes attributes;
    private OrganizationId organizationId;
    private final List<ProductVariantId> variantIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private ProductTemplate(ProductTemplateId id) {
        this.id = id;
        this.variantIds = new ArrayList<>();
        this.active = true;
        this.requiresExpiry = false;
        this.attributes = ProductAttributes.empty();
    }

    @Override
    public ProductTemplateId getId() {
        return id;
    }

    /**
     * Factory method to create a new product template.
     */
    public static ProductTemplate create(
            String name,
            String description,
            CategoryId categoryId,
            UnitOfMeasureId unitOfMeasureId,
            BrandId brandId,
            String imageUrl,
            Integer reorderPoint,
            boolean requiresExpiry,
            ProductAttributes attributes
    ) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(categoryId, "Category is required");
        Objects.requireNonNull(unitOfMeasureId, "Unit of measure is required");

        TenantContext tenant = TenantContext.current();

        ProductTemplate template = new ProductTemplate(ProductTemplateId.generate());
        template.name = name.trim();
        template.description = description;
        template.categoryId = categoryId;
        template.unitOfMeasureId = unitOfMeasureId;
        template.brandId = brandId;
        template.imageUrl = imageUrl;
        template.reorderPoint = reorderPoint != null ? reorderPoint : 10;
        template.requiresExpiry = requiresExpiry;
        template.attributes = attributes != null ? attributes : ProductAttributes.empty();
        template.organizationId = OrganizationId.of(tenant.organizationId());
        template.createdAt = LocalDateTime.now();
        template.updatedAt = template.createdAt;

        template.registerEvent(new ProductTemplateCreated(
                template.id,
                template.name,
                template.categoryId,
                template.organizationId
        ));

        return template;
    }

    /**
     * Reconstitute from persistence.
     */
    public static ProductTemplate reconstitute(
            ProductTemplateId id,
            String name,
            String description,
            CategoryId categoryId,
            BrandId brandId,
            UnitOfMeasureId unitOfMeasureId,
            String imageUrl,
            Integer reorderPoint,
            boolean requiresExpiry,
            boolean active,
            ProductAttributes attributes,
            OrganizationId organizationId,
            List<ProductVariantId> variantIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        ProductTemplate template = new ProductTemplate(id);
        template.name = name;
        template.description = description;
        template.categoryId = categoryId;
        template.brandId = brandId;
        template.unitOfMeasureId = unitOfMeasureId;
        template.imageUrl = imageUrl;
        template.reorderPoint = reorderPoint;
        template.requiresExpiry = requiresExpiry;
        template.active = active;
        template.attributes = attributes != null ? attributes : ProductAttributes.empty();
        template.organizationId = organizationId;
        if (variantIds != null) {
            template.variantIds.addAll(variantIds);
        }
        template.createdAt = createdAt;
        template.updatedAt = updatedAt;
        template.deletedAt = deletedAt;
        return template;
    }

    // ==================== Commands ====================

    public void updateDetails(
            String name,
            String description,
            CategoryId categoryId,
            UnitOfMeasureId unitOfMeasureId,
            BrandId brandId,
            String imageUrl,
            Integer reorderPoint,
            Boolean requiresExpiry
    ) {
        StringBuilder updatedFields = new StringBuilder();

        if (name != null && !name.equals(this.name)) {
            this.name = name.trim();
            updatedFields.append("name,");
        }
        if (description != null && !description.equals(this.description)) {
            this.description = description;
            updatedFields.append("description,");
        }
        if (categoryId != null && !categoryId.equals(this.categoryId)) {
            this.categoryId = categoryId;
            updatedFields.append("category,");
        }
        if (unitOfMeasureId != null && !unitOfMeasureId.equals(this.unitOfMeasureId)) {
            this.unitOfMeasureId = unitOfMeasureId;
            updatedFields.append("unitOfMeasure,");
        }
        if (brandId != null) {
            this.brandId = brandId;
            updatedFields.append("brand,");
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
            updatedFields.append("imageUrl,");
        }
        if (reorderPoint != null && !reorderPoint.equals(this.reorderPoint)) {
            this.reorderPoint = reorderPoint;
            updatedFields.append("reorderPoint,");
        }
        if (requiresExpiry != null && requiresExpiry != this.requiresExpiry) {
            this.requiresExpiry = requiresExpiry;
            updatedFields.append("requiresExpiry,");
        }

        this.updatedAt = LocalDateTime.now();

        if (updatedFields.length() > 0) {
            registerEvent(new ProductTemplateUpdated(
                    this.id,
                    updatedFields.toString()
            ));
        }
    }

    public void updateAttributes(ProductAttributes attributes) {
        this.attributes = attributes != null ? attributes : ProductAttributes.empty();
        this.updatedAt = LocalDateTime.now();
        registerEvent(new ProductTemplateUpdated(id, "attributes"));
    }

    public void activate() {
        if (!this.active) {
            this.active = true;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new ProductTemplateUpdated(id, "activated"));
        }
    }

    public void deactivate() {
        if (this.active) {
            this.active = false;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new ProductTemplateUpdated(id, "deactivated"));
        }
    }

    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.updatedAt = this.deletedAt;
            registerEvent(new ProductTemplateDeleted(id));
        }
    }

    public void addVariant(ProductVariantId variantId) {
        if (!variantIds.contains(variantId)) {
            variantIds.add(variantId);
        }
    }

    public void removeVariant(ProductVariantId variantId) {
        variantIds.remove(variantId);
    }

    // ==================== Queries ====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean hasVariants() {
        return !variantIds.isEmpty();
    }

    public int getVariantCount() {
        return variantIds.size();
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CategoryId getCategoryId() {
        return categoryId;
    }

    public BrandId getBrandId() {
        return brandId;
    }

    public UnitOfMeasureId getUnitOfMeasureId() {
        return unitOfMeasureId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public boolean isRequiresExpiry() {
        return requiresExpiry;
    }

    public boolean isActive() {
        return active;
    }

    public ProductAttributes getAttributes() {
        return attributes;
    }

    public OrganizationId getOrganizationId() {
        return organizationId;
    }

    public List<ProductVariantId> getVariantIds() {
        return Collections.unmodifiableList(variantIds);
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
