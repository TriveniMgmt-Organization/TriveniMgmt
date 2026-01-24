package com.store.mgmt.modules.users.infrastructure.persistence;

import com.store.mgmt.modules.users.domain.model.*;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JPA implementation of RoleRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleRoleRepository")
public class JpaRoleRepository implements RoleRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Role> findById(RoleId id) {
        try {
            TypedQuery<com.store.mgmt.users.model.entity.Role> query = em.createQuery("""
                SELECT DISTINCT r FROM Role r
                LEFT JOIN FETCH r.permissions
                WHERE r.id = :id
                AND r.deletedAt IS NULL
                """, com.store.mgmt.users.model.entity.Role.class);
            query.setParameter("id", id.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Role> findByName(String name) {
        try {
            TypedQuery<com.store.mgmt.users.model.entity.Role> query = em.createQuery("""
                SELECT r FROM Role r
                WHERE r.name = :name
                AND r.deletedAt IS NULL
                """, com.store.mgmt.users.model.entity.Role.class);
            query.setParameter("name", name);

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Role> findAll() {
        TypedQuery<com.store.mgmt.users.model.entity.Role> query = em.createQuery("""
            SELECT r FROM Role r
            WHERE r.deletedAt IS NULL
            ORDER BY r.name ASC
            """, com.store.mgmt.users.model.entity.Role.class);

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(r) FROM Role r
            WHERE r.name = :name
            AND r.deletedAt IS NULL
            """, Long.class);
        query.setParameter("name", name);

        return query.getSingleResult() > 0;
    }

    @Override
    public Role save(Role role) {
        com.store.mgmt.users.model.entity.Role entity = toEntity(role);

        if (em.find(com.store.mgmt.users.model.entity.Role.class, role.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(Role role) {
        com.store.mgmt.users.model.entity.Role entity =
                em.find(com.store.mgmt.users.model.entity.Role.class, role.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(role.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private Role toDomain(com.store.mgmt.users.model.entity.Role entity) {
        Set<PermissionId> permissionIds = new HashSet<>();
        if (entity.getPermissions() != null) {
            entity.getPermissions().forEach(p -> permissionIds.add(PermissionId.of(p.getId())));
        }

        return Role.reconstitute(
                RoleId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                permissionIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.users.model.entity.Role toEntity(Role domain) {
        com.store.mgmt.users.model.entity.Role entity =
                em.find(com.store.mgmt.users.model.entity.Role.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.users.model.entity.Role();
            entity.setId(domain.getId().getValue());
        }

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        // Handle permissions - load and set
        if (domain.getPermissionIds() != null && !domain.getPermissionIds().isEmpty()) {
            Set<com.store.mgmt.users.model.entity.Permission> permissions = new HashSet<>();
            for (PermissionId pid : domain.getPermissionIds()) {
                com.store.mgmt.users.model.entity.Permission perm =
                        em.find(com.store.mgmt.users.model.entity.Permission.class, pid.getValue());
                if (perm != null) {
                    permissions.add(perm);
                }
            }
            entity.setPermissions(permissions);
        }

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
