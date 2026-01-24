package com.store.mgmt.modules.users.infrastructure.persistence;

import com.store.mgmt.modules.users.domain.model.*;
import com.store.mgmt.modules.users.domain.repository.PermissionRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of PermissionRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("modulePermissionRepository")
public class JpaPermissionRepository implements PermissionRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Permission> findById(PermissionId id) {
        com.store.mgmt.users.model.entity.Permission entity =
                em.find(com.store.mgmt.users.model.entity.Permission.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public Optional<Permission> findByName(String name) {
        try {
            TypedQuery<com.store.mgmt.users.model.entity.Permission> query = em.createQuery("""
                SELECT p FROM Permission p
                WHERE p.name = :name
                AND p.deletedAt IS NULL
                """, com.store.mgmt.users.model.entity.Permission.class);
            query.setParameter("name", name);

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Permission> findAll() {
        TypedQuery<com.store.mgmt.users.model.entity.Permission> query = em.createQuery("""
            SELECT p FROM Permission p
            WHERE p.deletedAt IS NULL
            ORDER BY p.name ASC
            """, com.store.mgmt.users.model.entity.Permission.class);

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(p) FROM Permission p
            WHERE p.name = :name
            AND p.deletedAt IS NULL
            """, Long.class);
        query.setParameter("name", name);

        return query.getSingleResult() > 0;
    }

    @Override
    public Permission save(Permission permission) {
        com.store.mgmt.users.model.entity.Permission entity = toEntity(permission);

        if (em.find(com.store.mgmt.users.model.entity.Permission.class, permission.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(Permission permission) {
        com.store.mgmt.users.model.entity.Permission entity =
                em.find(com.store.mgmt.users.model.entity.Permission.class, permission.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(permission.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private Permission toDomain(com.store.mgmt.users.model.entity.Permission entity) {
        return Permission.reconstitute(
                PermissionId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.users.model.entity.Permission toEntity(Permission domain) {
        com.store.mgmt.users.model.entity.Permission entity =
                em.find(com.store.mgmt.users.model.entity.Permission.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.users.model.entity.Permission();
            entity.setId(domain.getId().getValue());
        }

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
