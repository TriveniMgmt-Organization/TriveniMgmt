package com.store.mgmt.modules.products.domain.repository;

import com.store.mgmt.modules.products.domain.model.Barcode;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.modules.products.domain.model.ProductVariant;
import com.store.mgmt.modules.products.domain.model.ProductVariantId;
import com.store.mgmt.modules.products.domain.model.Sku;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductVariant aggregate.
 * This is a domain port - implementation is in infrastructure layer.
 */
public interface ProductVariantRepository {

    /**
     * Find a variant by ID.
     */
    Optional<ProductVariant> findById(ProductVariantId id);

    /**
     * Find a variant by ID and organization (tenant isolation).
     */
    Optional<ProductVariant> findByIdAndOrganizationId(ProductVariantId id, OrganizationId organizationId);

    /**
     * Find a variant by SKU within an organization.
     */
    Optional<ProductVariant> findBySkuAndOrganizationId(Sku sku, OrganizationId organizationId);

    /**
     * Find a variant by barcode within an organization.
     */
    Optional<ProductVariant> findByBarcodeAndOrganizationId(Barcode barcode, OrganizationId organizationId);

    /**
     * Find all variants for a product template.
     */
    List<ProductVariant> findByTemplateId(ProductTemplateId templateId);

    /**
     * Find active variants for a product template.
     */
    List<ProductVariant> findActiveByTemplateId(ProductTemplateId templateId);

    /**
     * Find all variants for an organization.
     */
    List<ProductVariant> findByOrganizationId(OrganizationId organizationId);

    /**
     * Check if SKU exists for an organization.
     */
    boolean existsBySkuAndOrganizationId(Sku sku, OrganizationId organizationId);

    /**
     * Check if barcode exists for an organization.
     */
    boolean existsByBarcodeAndOrganizationId(Barcode barcode, OrganizationId organizationId);

    /**
     * Save a product variant.
     */
    ProductVariant save(ProductVariant variant);

    /**
     * Delete a product variant (soft delete).
     */
    void delete(ProductVariant variant);
}
