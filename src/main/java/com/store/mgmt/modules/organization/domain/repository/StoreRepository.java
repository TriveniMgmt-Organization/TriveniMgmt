package com.store.mgmt.modules.organization.domain.repository;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Store aggregate.
 */
public interface StoreRepository {

    /**
     * Find a store by ID.
     */
    Optional<Store> findById(StoreId id);

    /**
     * Find a store by name within an organization.
     */
    Optional<Store> findByNameAndOrganizationId(String name, OrganizationId organizationId);

    /**
     * Find all stores for an organization.
     */
    List<Store> findByOrganizationId(OrganizationId organizationId);

    /**
     * Find all stores.
     */
    List<Store> findAll();

    /**
     * Check if a store name exists within an organization.
     */
    boolean existsByNameAndOrganizationId(String name, OrganizationId organizationId);

    /**
     * Save a store.
     */
    Store save(Store store);

    /**
     * Delete a store (soft delete).
     */
    void delete(Store store);
}
