package com.store.mgmt.modules.organization.infrastructure.persistence;

import com.store.mgmt.modules.organization.domain.model.*;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of StoreRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleStoreRepository")
public class JpaStoreRepository implements StoreRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Store> findById(StoreId id) {
        com.store.mgmt.organization.model.entity.Store entity =
                em.find(com.store.mgmt.organization.model.entity.Store.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public Optional<Store> findByNameAndOrganizationId(String name, OrganizationId organizationId) {
        try {
            TypedQuery<com.store.mgmt.organization.model.entity.Store> query = em.createQuery("""
                SELECT s FROM Store s
                WHERE s.name = :name
                AND s.organization.id = :orgId
                AND s.deletedAt IS NULL
                """, com.store.mgmt.organization.model.entity.Store.class);
            query.setParameter("name", name);
            query.setParameter("orgId", organizationId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Store> findByOrganizationId(OrganizationId organizationId) {
        TypedQuery<com.store.mgmt.organization.model.entity.Store> query = em.createQuery("""
            SELECT s FROM Store s
            WHERE s.organization.id = :orgId
            AND s.deletedAt IS NULL
            ORDER BY s.createdAt DESC
            """, com.store.mgmt.organization.model.entity.Store.class);
        query.setParameter("orgId", organizationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Store> findAll() {
        TypedQuery<com.store.mgmt.organization.model.entity.Store> query = em.createQuery("""
            SELECT s FROM Store s
            WHERE s.deletedAt IS NULL
            ORDER BY s.createdAt DESC
            """, com.store.mgmt.organization.model.entity.Store.class);

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameAndOrganizationId(String name, OrganizationId organizationId) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(s) FROM Store s
            WHERE s.name = :name
            AND s.organization.id = :orgId
            AND s.deletedAt IS NULL
            """, Long.class);
        query.setParameter("name", name);
        query.setParameter("orgId", organizationId.getValue());

        return query.getSingleResult() > 0;
    }

    @Override
    public Store save(Store store) {
        com.store.mgmt.organization.model.entity.Store entity = toEntity(store);

        if (em.find(com.store.mgmt.organization.model.entity.Store.class, store.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(Store store) {
        com.store.mgmt.organization.model.entity.Store entity =
                em.find(com.store.mgmt.organization.model.entity.Store.class, store.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(store.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private Store toDomain(com.store.mgmt.organization.model.entity.Store entity) {
        return Store.reconstitute(
                StoreId.of(entity.getId()),
                OrganizationId.of(entity.getOrganization().getId()),
                entity.getName(),
                entity.getLocation(),
                entity.getCountryCode(),
                entity.getContactInfo() != null ? ContactInfo.of(entity.getContactInfo()) : null,
                mapStatus(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private StoreStatus mapStatus(com.store.mgmt.organization.enums.StoreStatus entityStatus) {
        if (entityStatus == null) {
            return StoreStatus.ACTIVE;
        }
        return switch (entityStatus) {
            case ACTIVE -> StoreStatus.ACTIVE;
            case INACTIVE -> StoreStatus.INACTIVE;
            case CLOSED -> StoreStatus.CLOSED;
        };
    }

    private com.store.mgmt.organization.enums.StoreStatus mapToEntityStatus(StoreStatus domainStatus) {
        return switch (domainStatus) {
            case ACTIVE -> com.store.mgmt.organization.enums.StoreStatus.ACTIVE;
            case INACTIVE -> com.store.mgmt.organization.enums.StoreStatus.INACTIVE;
            case CLOSED -> com.store.mgmt.organization.enums.StoreStatus.CLOSED;
        };
    }

    private com.store.mgmt.organization.model.entity.Store toEntity(Store domain) {
        com.store.mgmt.organization.model.entity.Store entity =
                em.find(com.store.mgmt.organization.model.entity.Store.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.organization.model.entity.Store();
            entity.setId(domain.getId().getValue());

            // Load organization reference
            com.store.mgmt.organization.model.entity.Organization org =
                    em.find(com.store.mgmt.organization.model.entity.Organization.class, domain.getOrganizationId().getValue());
            entity.setOrganization(org);
        }

        entity.setName(domain.getName());
        entity.setLocation(domain.getLocation());
        entity.setCountryCode(domain.getCountryCode());
        entity.setContactInfo(domain.getContactInfo() != null ? domain.getContactInfo().getValue() : null);
        entity.setStatus(mapToEntityStatus(domain.getStatus()));

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
