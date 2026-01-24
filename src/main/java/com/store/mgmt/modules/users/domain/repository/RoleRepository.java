package com.store.mgmt.modules.users.domain.repository;

import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.RoleId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Role aggregate.
 */
public interface RoleRepository {

    /**
     * Find a role by ID.
     */
    Optional<Role> findById(RoleId id);

    /**
     * Find a role by name.
     */
    Optional<Role> findByName(String name);

    /**
     * Find all roles.
     */
    List<Role> findAll();

    /**
     * Check if a role with the given name exists.
     */
    boolean existsByName(String name);

    /**
     * Save a role.
     */
    Role save(Role role);

    /**
     * Delete a role (soft delete).
     */
    void delete(Role role);
}
