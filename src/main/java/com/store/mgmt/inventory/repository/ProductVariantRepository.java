package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    
    // Find by SKU within organization (unique constraint)
    Optional<ProductVariant> findBySkuAndOrganizationId(String sku, UUID organizationId);
    
    // Find by barcode within organization (unique constraint)
    Optional<ProductVariant> findByBarcodeAndOrganizationId(String barcode, UUID organizationId);
    
    // Find all variants for a template
    List<ProductVariant> findByTemplateId(UUID templateId);
    
    // Find all variants for an organization
    @Query("SELECT v FROM ProductVariant v WHERE v.organization.id = :organizationId AND v.deletedAt IS NULL")
    List<ProductVariant> findByOrganizationId(@Param("organizationId") UUID organizationId);
    
    // Find active variants for a template
    @Query("SELECT v FROM ProductVariant v WHERE v.template.id = :templateId AND v.isActive = true AND v.deletedAt IS NULL")
    List<ProductVariant> findActiveByTemplateId(@Param("templateId") UUID templateId);
    
    // Find variant by ID and organization (for security)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id AND v.organization.id = :organizationId AND v.deletedAt IS NULL")
    Optional<ProductVariant> findByIdAndOrganizationId(@Param("id") UUID id, @Param("organizationId") UUID organizationId);
}

