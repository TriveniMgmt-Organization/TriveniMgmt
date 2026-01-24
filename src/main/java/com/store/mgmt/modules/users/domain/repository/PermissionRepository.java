package com.store.mgmt.modules.users.domain.repository;

import com.store.mgmt.modules.users.domain.model.Permission;
import com.store.mgmt.modules.users.domain.model.PermissionId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Permission entity.
 */
public interface PermissionRepository {

    /**
     * Find a permission by ID.
     */
    Optional<Permission> findById(PermissionId id);

    /**
     * Find a permission by name.
     */
    Optional<Permission> findByName(String name);

    /**
     * Find all permissions.
     */
    List<Permission> findAll();

    /**
     * Check if a permission with the given name exists.
     */
    boolean existsByName(String name);

    /**
     * Save a permission.
     */
    Permission save(Permission permission);

    /**
     * Delete a permission (soft delete).
     */
    void delete(Permission permission);
}
