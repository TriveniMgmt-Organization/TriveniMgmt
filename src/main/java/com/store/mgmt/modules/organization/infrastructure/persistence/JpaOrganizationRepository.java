package com.store.mgmt.modules.organization.infrastructure.persistence;

import com.store.mgmt.modules.organization.domain.model.*;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
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
 * JPA implementation of OrganizationRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleOrganizationRepository")
public class JpaOrganizationRepository implements OrganizationRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Organization> findById(OrganizationId id) {
        try {
            TypedQuery<com.store.mgmt.organization.model.entity.Organization> query = em.createQuery("""
                SELECT DISTINCT o FROM Organization o
                LEFT JOIN FETCH o.stores s
                WHERE o.id = :id
                AND o.deletedAt IS NULL
                AND (s.deletedAt IS NULL OR s IS NULL)
                """, com.store.mgmt.organization.model.entity.Organization.class);
            query.setParameter("id", id.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Organization> findByName(String name) {
        try {
            TypedQuery<com.store.mgmt.organization.model.entity.Organization> query = em.createQuery("""
                SELECT o FROM Organization o
                WHERE o.name = :name
                AND o.deletedAt IS NULL
                """, com.store.mgmt.organization.model.entity.Organization.class);
            query.setParameter("name", name);

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Organization> findAll() {
        TypedQuery<com.store.mgmt.organization.model.entity.Organization> query = em.createQuery("""
            SELECT o FROM Organization o
            WHERE o.deletedAt IS NULL
            ORDER BY o.createdAt DESC
            """, com.store.mgmt.organization.model.entity.Organization.class);

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Organization> findAllByUserId(UserId userId) {
        TypedQuery<com.store.mgmt.organization.model.entity.Organization> query = em.createQuery("""
            SELECT o FROM Organization o
            JOIN o.userRoles ur
            WHERE ur.user.id = :userId
            AND o.deletedAt IS NULL
            ORDER BY o.createdAt DESC
            """, com.store.mgmt.organization.model.entity.Organization.class);
        query.setParameter("userId", userId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(o) FROM Organization o
            WHERE o.name = :name
            AND o.deletedAt IS NULL
            """, Long.class);
        query.setParameter("name", name);

        return query.getSingleResult() > 0;
    }

    @Override
    public Organization save(Organization organization) {
        com.store.mgmt.organization.model.entity.Organization entity = toEntity(organization);

        if (em.find(com.store.mgmt.organization.model.entity.Organization.class, organization.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(Organization organization) {
        com.store.mgmt.organization.model.entity.Organization entity =
                em.find(com.store.mgmt.organization.model.entity.Organization.class, organization.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(organization.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private Organization toDomain(com.store.mgmt.organization.model.entity.Organization entity) {
        List<StoreId> storeIds = new ArrayList<>();
        if (entity.getStores() != null) {
            entity.getStores().stream()
                    .filter(s -> s.getDeletedAt() == null)
                    .forEach(s -> storeIds.add(StoreId.of(s.getId())));
        }

        return Organization.reconstitute(
                OrganizationId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getContactInfo() != null ? ContactInfo.of(entity.getContactInfo()) : null,
                entity.getAppliedTemplateCode(),
                storeIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.organization.model.entity.Organization toEntity(Organization domain) {
        com.store.mgmt.organization.model.entity.Organization entity =
                em.find(com.store.mgmt.organization.model.entity.Organization.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.organization.model.entity.Organization();
            entity.setId(domain.getId().getValue());
        }

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setContactInfo(domain.getContactInfo() != null ? domain.getContactInfo().getValue() : null);
        entity.setAppliedTemplateCode(domain.getAppliedTemplateCode());

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
