package com.store.mgmt.modules.products.domain.repository;

import com.store.mgmt.modules.products.domain.model.CategoryId;
import com.store.mgmt.modules.products.domain.model.OrganizationId;
import com.store.mgmt.modules.products.domain.model.ProductTemplate;
import com.store.mgmt.modules.products.domain.model.ProductTemplateId;
import com.store.mgmt.modules.products.domain.model.UnitOfMeasureId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductTemplate aggregate.
 * This is a domain port - implementation is in infrastructure layer.
 */
public interface ProductTemplateRepository {

    /**
     * Find a product template by ID.
     */
    Optional<ProductTemplate> findById(ProductTemplateId id);

    /**
     * Find a product template by ID and organization (tenant isolation).
     */
    Optional<ProductTemplate> findByIdAndOrganizationId(ProductTemplateId id, OrganizationId organizationId);

    /**
     * Find all product templates for an organization.
     */
    List<ProductTemplate> findByOrganizationId(OrganizationId organizationId);

    /**
     * Find all product templates by category.
     */
    List<ProductTemplate> findByCategoryIdAndOrganizationId(CategoryId categoryId, OrganizationId organizationId);

    /**
     * Find all product templates by unit of measure.
     */
    List<ProductTemplate> findByUnitOfMeasureIdAndOrganizationId(UnitOfMeasureId unitOfMeasureId, OrganizationId organizationId);

    /**
     * Find active product templates for an organization.
     */
    List<ProductTemplate> findActiveByOrganizationId(OrganizationId organizationId);

    /**
     * Check if a product template name exists for an organization.
     */
    boolean existsByNameAndOrganizationId(String name, OrganizationId organizationId);

    /**
     * Save a product template.
     */
    ProductTemplate save(ProductTemplate template);

    /**
     * Delete a product template (soft delete).
     */
    void delete(ProductTemplate template);
}
