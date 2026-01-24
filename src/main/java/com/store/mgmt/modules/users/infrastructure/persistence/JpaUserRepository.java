package com.store.mgmt.modules.users.infrastructure.persistence;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.modules.users.domain.model.*;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
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
 * JPA implementation of UserRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleUserRepository")
public class JpaUserRepository implements UserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<User> findById(UserId id) {
        try {
            TypedQuery<com.store.mgmt.users.model.entity.User> query = em.createQuery("""
                SELECT DISTINCT u FROM User u
                LEFT JOIN FETCH u.organizationRoles ur
                LEFT JOIN FETCH ur.role r
                LEFT JOIN FETCH ur.organization
                LEFT JOIN FETCH ur.store
                WHERE u.id = :id
                AND u.deletedAt IS NULL
                """, com.store.mgmt.users.model.entity.User.class);
            query.setParameter("id", id.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        try {
            TypedQuery<com.store.mgmt.users.model.entity.User> query = em.createQuery("""
                SELECT u FROM User u
                WHERE u.email = :email
                AND u.deletedAt IS NULL
                """, com.store.mgmt.users.model.entity.User.class);
            query.setParameter("email", email.value());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        try {
            TypedQuery<com.store.mgmt.users.model.entity.User> query = em.createQuery("""
                SELECT u FROM User u
                WHERE u.username = :username
                AND u.deletedAt IS NULL
                """, com.store.mgmt.users.model.entity.User.class);
            query.setParameter("username", username.value());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        TypedQuery<com.store.mgmt.users.model.entity.User> query = em.createQuery("""
            SELECT u FROM User u
            WHERE u.deletedAt IS NULL
            ORDER BY u.createdAt DESC
            """, com.store.mgmt.users.model.entity.User.class);

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(Email email) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(u) FROM User u
            WHERE u.email = :email
            AND u.deletedAt IS NULL
            """, Long.class);
        query.setParameter("email", email.value());

        return query.getSingleResult() > 0;
    }

    @Override
    public boolean existsByUsername(Username username) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(u) FROM User u
            WHERE u.username = :username
            AND u.deletedAt IS NULL
            """, Long.class);
        query.setParameter("username", username.value());

        return query.getSingleResult() > 0;
    }

    @Override
    public User save(User user) {
        com.store.mgmt.users.model.entity.User entity = toEntity(user);

        if (em.find(com.store.mgmt.users.model.entity.User.class, user.getId().getValue()) == null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(User user) {
        com.store.mgmt.users.model.entity.User entity =
                em.find(com.store.mgmt.users.model.entity.User.class, user.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(user.getDeletedAt());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private User toDomain(com.store.mgmt.users.model.entity.User entity) {
        Set<UserOrganizationRole> roles = new HashSet<>();
        if (entity.getOrganizationRoles() != null) {
            entity.getOrganizationRoles().forEach(ur -> {
                roles.add(new UserOrganizationRole(
                        RoleId.of(ur.getRole().getId()),
                        OrganizationId.of(ur.getOrganization().getId()),
                        ur.getStore() != null ? StoreId.of(ur.getStore().getId()) : null
                ));
            });
        }

        return User.reconstitute(
                UserId.of(entity.getId()),
                Username.of(entity.getUsername()),
                Email.of(entity.getEmail()),
                entity.getPasswordHash(),
                PersonName.of(entity.getFirstName(), entity.getLastName()),
                entity.getImageUrl(),
                entity.isActive(),
                roles,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.users.model.entity.User toEntity(User domain) {
        com.store.mgmt.users.model.entity.User entity =
                em.find(com.store.mgmt.users.model.entity.User.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.users.model.entity.User();
            entity.setId(domain.getId().getValue());
        }

        entity.setUsername(domain.getUsername().value());
        entity.setEmail(domain.getEmail().value());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setFirstName(domain.getName() != null ? domain.getName().firstName() : null);
        entity.setLastName(domain.getName() != null ? domain.getName().lastName() : null);
        entity.setImageUrl(domain.getImageUrl());
        entity.setActive(domain.isActive());

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
