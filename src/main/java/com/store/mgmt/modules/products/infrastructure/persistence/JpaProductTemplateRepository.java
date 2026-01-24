package com.store.mgmt.modules.products.infrastructure.persistence;

import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductTemplateRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of ProductTemplateRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleProductTemplateRepository")
public class JpaProductTemplateRepository implements ProductTemplateRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ProductTemplate> findById(ProductTemplateId id) {
        com.store.mgmt.modules.inventory.domain.model.ProductTemplate entity =
                em.find(com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public Optional<ProductTemplate> findByIdAndOrganizationId(ProductTemplateId id, OrganizationId organizationId) {
        try {
            TypedQuery<com.store.mgmt.modules.inventory.domain.model.ProductTemplate> query = em.createQuery("""
                SELECT pt FROM ProductTemplate pt
                LEFT JOIN FETCH pt.category
                LEFT JOIN FETCH pt.brand
                LEFT JOIN FETCH pt.unitOfMeasure
                WHERE pt.id = :id
                AND pt.organizationId = :orgId
                AND pt.deletedAt IS NULL
                """, com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class);
            query.setParameter("id", id.getValue());
            query.setParameter("orgId", organizationId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ProductTemplate> findByOrganizationId(OrganizationId organizationId) {
        TypedQuery<com.store.mgmt.modules.inventory.domain.model.ProductTemplate> query = em.createQuery("""
            SELECT DISTINCT pt FROM ProductTemplate pt
            LEFT JOIN FETCH pt.category
            LEFT JOIN FETCH pt.brand
            LEFT JOIN FETCH pt.unitOfMeasure
            WHERE pt.organizationId = :orgId
            AND pt.deletedAt IS NULL
            ORDER BY pt.name ASC
            """, com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class);
        query.setParameter("orgId", organizationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductTemplate> findByCategoryIdAndOrganizationId(CategoryId categoryId, OrganizationId organizationId) {
        TypedQuery<com.store.mgmt.modules.inventory.domain.model.ProductTemplate> query = em.createQuery("""
            SELECT DISTINCT pt FROM ProductTemplate pt
            LEFT JOIN FETCH pt.category
            LEFT JOIN FETCH pt.brand
            LEFT JOIN FETCH pt.unitOfMeasure
            WHERE pt.category.id = :categoryId
            AND pt.organizationId = :orgId
            AND pt.deletedAt IS NULL
            ORDER BY pt.name ASC
            """, com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class);
        query.setParameter("categoryId", categoryId.getValue());
        query.setParameter("orgId", organizationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductTemplate> findByUnitOfMeasureIdAndOrganizationId(UnitOfMeasureId unitOfMeasureId, OrganizationId organizationId) {
        TypedQuery<com.store.mgmt.modules.inventory.domain.model.ProductTemplate> query = em.createQuery("""
            SELECT DISTINCT pt FROM ProductTemplate pt
            LEFT JOIN FETCH pt.category
            LEFT JOIN FETCH pt.brand
            LEFT JOIN FETCH pt.unitOfMeasure
            WHERE pt.unitOfMeasure.id = :uomId
            AND pt.organizationId = :orgId
            AND pt.deletedAt IS NULL
            ORDER BY pt.name ASC
            """, com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class);
        query.setParameter("uomId", unitOfMeasureId.getValue());
        query.setParameter("orgId", organizationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductTemplate> findActiveByOrganizationId(OrganizationId organizationId) {
        TypedQuery<com.store.mgmt.modules.inventory.domain.model.ProductTemplate> query = em.createQuery("""
            SELECT DISTINCT pt FROM ProductTemplate pt
            LEFT JOIN FETCH pt.category
            LEFT JOIN FETCH pt.brand
            LEFT JOIN FETCH pt.unitOfMeasure
            WHERE pt.organizationId = :orgId
            AND pt.isActive = true
            AND pt.deletedAt IS NULL
            ORDER BY pt.name ASC
            """, com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class);
        query.setParameter("orgId", organizationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameAndOrganizationId(String name, OrganizationId organizationId) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(pt) FROM ProductTemplate pt
            WHERE pt.name = :name
            AND pt.organizationId = :orgId
            AND pt.deletedAt IS NULL
            """, Long.class);
        query.setParameter("name", name);
        query.setParameter("orgId", organizationId.getValue());

        return query.getSingleResult() > 0;
    }

    @Override
    public ProductTemplate save(ProductTemplate template) {
        com.store.mgmt.modules.inventory.domain.model.ProductTemplate entity = toEntity(template);

        if (em.find(com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class, template.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(ProductTemplate template) {
        com.store.mgmt.modules.inventory.domain.model.ProductTemplate entity =
                em.find(com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class, template.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(template.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private ProductTemplate toDomain(com.store.mgmt.modules.inventory.domain.model.ProductTemplate entity) {
        List<ProductVariantId> variantIds = new ArrayList<>();
        if (entity.getVariants() != null) {
            entity.getVariants().forEach(v -> variantIds.add(ProductVariantId.of(v.getId())));
        }

        return ProductTemplate.reconstitute(
                ProductTemplateId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory() != null ? CategoryId.of(entity.getCategory().getId()) : null,
                entity.getBrand() != null ? BrandId.of(entity.getBrand().getId()) : null,
                entity.getUnitOfMeasure() != null ? UnitOfMeasureId.of(entity.getUnitOfMeasure().getId()) : null,
                entity.getImageUrl(),
                entity.getReorderPoint(),
                entity.isRequiresExpiry(),
                entity.isActive(),
                ProductAttributes.of(entity.getAttributes()),
                OrganizationId.of(entity.getOrganizationId()),
                variantIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.modules.inventory.domain.model.ProductTemplate toEntity(ProductTemplate domain) {
        com.store.mgmt.modules.inventory.domain.model.ProductTemplate entity =
                em.find(com.store.mgmt.modules.inventory.domain.model.ProductTemplate.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.modules.inventory.domain.model.ProductTemplate();
            entity.setId(domain.getId().getValue());
            entity.setOrganizationId(domain.getOrganizationId().getValue());
        }

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setImageUrl(domain.getImageUrl());
        entity.setReorderPoint(domain.getReorderPoint());
        entity.setRequiresExpiry(domain.isRequiresExpiry());
        entity.setActive(domain.isActive());

        if (domain.getAttributes() != null) {
            entity.setAttributes(domain.getAttributes().getAll());
        }

        // Load references
        if (domain.getCategoryId() != null) {
            com.store.mgmt.modules.inventory.domain.model.Category category =
                    em.find(com.store.mgmt.modules.inventory.domain.model.Category.class, domain.getCategoryId().getValue());
            entity.setCategory(category);
        }

        if (domain.getBrandId() != null) {
            com.store.mgmt.modules.inventory.domain.model.Brand brand =
                    em.find(com.store.mgmt.modules.inventory.domain.model.Brand.class, domain.getBrandId().getValue());
            entity.setBrand(brand);
        }

        if (domain.getUnitOfMeasureId() != null) {
            com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure uom =
                    em.find(com.store.mgmt.modules.inventory.domain.model.UnitOfMeasure.class, domain.getUnitOfMeasureId().getValue());
            entity.setUnitOfMeasure(uom);
        }

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
