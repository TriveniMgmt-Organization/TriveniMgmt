package com.store.mgmt.modules.users.infrastructure.persistence.repository;

import com.store.mgmt.modules.users.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<User> findById(@NonNull UUID id);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<User> findAll();

    /**
     * @deprecated Use findAll() and fetch roles separately via UserOrganizationRoleRepository
     */
    @Deprecated
    default List<User> findAllWithRolesAndPermissions() {
        return findAll();
    }

    /**
     * @deprecated Use findById() and fetch roles separately via UserOrganizationRoleRepository
     */
    @Deprecated
    default Optional<User> findByIdWithRolesAndPermissions(UUID id) {
        return findById(id);
    }

    /**
     * @deprecated Use findByUsername() and fetch roles separately via UserOrganizationRoleRepository
     */
    @Deprecated
    default Optional<User> findByUsernameWithAllRelatedData(String username) {
        return findByUsername(username);
    }

    /**
     * @deprecated Use findByEmail() and fetch roles separately via UserOrganizationRoleRepository
     */
    @Deprecated
    default Optional<User> findByEmailWithRolesAndPermissions(String email) {
        return findByEmail(email);
    }

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
