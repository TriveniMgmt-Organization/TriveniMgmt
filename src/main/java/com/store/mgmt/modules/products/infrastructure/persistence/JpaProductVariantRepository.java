package com.store.mgmt.modules.products.infrastructure.persistence;

import com.store.mgmt.modules.products.domain.model.*;
import com.store.mgmt.modules.products.domain.repository.ProductVariantRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of ProductVariantRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleProductVariantRepository")
public class JpaProductVariantRepository implements ProductVariantRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ProductVariant> findById(ProductVariantId id) {
        com.store.mgmt.inventory.model.entity.ProductVariant entity =
                em.find(com.store.mgmt.inventory.model.entity.ProductVariant.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public Optional<ProductVariant> findByIdAndOrganizationId(ProductVariantId id, OrganizationId organizationId) {
        try {
            TypedQuery<com.store.mgmt.inventory.model.entity.ProductVariant> query = em.createQuery("""
                SELECT pv FROM ProductVariant pv
                LEFT JOIN FETCH pv.template
                WHERE pv.id = :id
                AND pv.organization.id = :orgId
                AND pv.deletedAt IS NULL
                """, com.store.mgmt.inventory.model.entity.ProductVariant.class);
            query.setParameter("id", id.getValue());
            query.setParameter("orgId", organizationId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProductVariant> findBySkuAndOrganizationId(Sku sku, OrganizationId organizationId) {
        try {
            TypedQuery<com.store.mgmt.inventory.model.entity.ProductVariant> query = em.createQuery("""
                SELECT pv FROM ProductVariant pv
                LEFT JOIN FETCH pv.template
                WHERE pv.sku = :sku
                AND pv.organization.id = :orgId
                AND pv.deletedAt IS NULL
                """, com.store.mgmt.inventory.model.entity.ProductVariant.class);
            query.setParameter("sku", sku.getValue());
            query.setParameter("orgId", organizationId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProductVariant> findByBarcodeAndOrganizationId(Barcode barcode, OrganizationId organizationId) {
        if (barcode == null) {
            return Optional.empty();
        }
        try {
            TypedQuery<com.store.mgmt.inventory.model.entity.ProductVariant> query = em.createQuery("""
                SELECT pv FROM ProductVariant pv
                LEFT JOIN FETCH pv.template
                WHERE pv.barcode = :barcode
                AND pv.organization.id = :orgId
                AND pv.deletedAt IS NULL
                """, com.store.mgmt.inventory.model.entity.ProductVariant.class);
            query.setParameter("barcode", barcode.getValue());
            query.setParameter("orgId", organizationId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ProductVariant> findByTemplateId(ProductTemplateId templateId) {
        TypedQuery<com.store.mgmt.inventory.model.entity.ProductVariant> query = em.createQuery("""
            SELECT pv FROM ProductVariant pv
            WHERE pv.template.id = :templateId
            AND pv.deletedAt IS NULL
            ORDER BY pv.sku ASC
            """, com.store.mgmt.inventory.model.entity.ProductVariant.class);
        query.setParameter("templateId", templateId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVariant> findActiveByTemplateId(ProductTemplateId templateId) {
        TypedQuery<com.store.mgmt.inventory.model.entity.ProductVariant> query = em.createQuery("""
            SELECT pv FROM ProductVariant pv
            WHERE pv.template.id = :templateId
            AND pv.isActive = true
            AND pv.deletedAt IS NULL
            ORDER BY pv.sku ASC
            """, com.store.mgmt.inventory.model.entity.ProductVariant.class);
        query.setParameter("templateId", templateId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVariant> findByOrganizationId(OrganizationId organizationId) {
        TypedQuery<com.store.mgmt.inventory.model.entity.ProductVariant> query = em.createQuery("""
            SELECT pv FROM ProductVariant pv
            LEFT JOIN FETCH pv.template
            WHERE pv.organization.id = :orgId
            AND pv.deletedAt IS NULL
            ORDER BY pv.sku ASC
            """, com.store.mgmt.inventory.model.entity.ProductVariant.class);
        query.setParameter("orgId", organizationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySkuAndOrganizationId(Sku sku, OrganizationId organizationId) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(pv) FROM ProductVariant pv
            WHERE pv.sku = :sku
            AND pv.organization.id = :orgId
            AND pv.deletedAt IS NULL
            """, Long.class);
        query.setParameter("sku", sku.getValue());
        query.setParameter("orgId", organizationId.getValue());

        return query.getSingleResult() > 0;
    }

    @Override
    public boolean existsByBarcodeAndOrganizationId(Barcode barcode, OrganizationId organizationId) {
        if (barcode == null) {
            return false;
        }
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(pv) FROM ProductVariant pv
            WHERE pv.barcode = :barcode
            AND pv.organization.id = :orgId
            AND pv.deletedAt IS NULL
            """, Long.class);
        query.setParameter("barcode", barcode.getValue());
        query.setParameter("orgId", organizationId.getValue());

        return query.getSingleResult() > 0;
    }

    @Override
    public ProductVariant save(ProductVariant variant) {
        com.store.mgmt.inventory.model.entity.ProductVariant entity = toEntity(variant);

        if (em.find(com.store.mgmt.inventory.model.entity.ProductVariant.class, variant.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(ProductVariant variant) {
        com.store.mgmt.inventory.model.entity.ProductVariant entity =
                em.find(com.store.mgmt.inventory.model.entity.ProductVariant.class, variant.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(variant.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private ProductVariant toDomain(com.store.mgmt.inventory.model.entity.ProductVariant entity) {
        return ProductVariant.reconstitute(
                ProductVariantId.of(entity.getId()),
                ProductTemplateId.of(entity.getTemplate().getId()),
                Sku.of(entity.getSku()),
                Barcode.of(entity.getBarcode()),
                Money.of(entity.getCostPrice()),
                Money.of(entity.getRetailPrice()),
                ProductAttributes.of(entity.getAttributeValues()),
                entity.isActive(),
                OrganizationId.of(entity.getOrganization().getId()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.inventory.model.entity.ProductVariant toEntity(ProductVariant domain) {
        com.store.mgmt.inventory.model.entity.ProductVariant entity =
                em.find(com.store.mgmt.inventory.model.entity.ProductVariant.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.inventory.model.entity.ProductVariant();
            entity.setId(domain.getId().getValue());

            // Load template reference
            com.store.mgmt.inventory.model.entity.ProductTemplate template =
                    em.find(com.store.mgmt.inventory.model.entity.ProductTemplate.class, domain.getTemplateId().getValue());
            entity.setTemplate(template);

            // Load organization reference
            com.store.mgmt.organization.model.entity.Organization org =
                    em.find(com.store.mgmt.organization.model.entity.Organization.class, domain.getOrganizationId().getValue());
            entity.setOrganization(org);
        }

        entity.setSku(domain.getSku().getValue());
        entity.setBarcode(domain.getBarcode() != null ? domain.getBarcode().getValue() : null);
        entity.setCostPrice(domain.getCostPrice().getAmount());
        entity.setRetailPrice(domain.getRetailPrice().getAmount());
        entity.setActive(domain.isActive());

        if (domain.getAttributeValues() != null) {
            entity.setAttributeValues(domain.getAttributeValues().getAll());
        }

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
