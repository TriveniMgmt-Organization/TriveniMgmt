package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.ProductTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, UUID> {
    @Query("SELECT u FROM ProductTemplate u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<ProductTemplate> findById(@org.springframework.lang.NonNull UUID id);
    @Query("SELECT u FROM ProductTemplate u WHERE u.category.id = :categoryId AND u.deletedAt IS NULL")
    @NonNull
    Optional<ProductTemplate> findByCategoryId(@org.springframework.lang.NonNull UUID categoryId);
    @Query("SELECT u FROM ProductTemplate u WHERE u.unitOfMeasure.id = :unitOfMeasureId AND u.deletedAt IS NULL")
    List<ProductTemplate> findByUnitOfMeasureId(@org.springframework.lang.NonNull UUID unitOfMeasureId);
    @Query("SELECT u FROM ProductTemplate u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<ProductTemplate> findAll();

    Optional<ProductTemplate> findByIdAndOrganizationId(UUID id, UUID organizationId);
    
    @Query("SELECT DISTINCT pt FROM ProductTemplate pt LEFT JOIN FETCH pt.variants v WHERE pt.organization.id = :organizationId AND pt.deletedAt IS NULL AND (v.deletedAt IS NULL OR v IS NULL) ORDER BY pt.createdAt DESC")
    List<ProductTemplate> findByOrganizationId(UUID organizationId);
    
    List<ProductTemplate> findByCategoryIdAndOrganizationId(UUID categoryId, UUID organizationId);
    List<ProductTemplate> findByUnitOfMeasureIdAndOrganizationId(UUID unitOfMeasureId, UUID organizationId);
}