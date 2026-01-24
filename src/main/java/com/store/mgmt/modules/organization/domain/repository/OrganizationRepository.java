package com.store.mgmt.modules.organization.domain.repository;

import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Organization aggregate.
 */
public interface OrganizationRepository {

    /**
     * Find an organization by ID.
     */
    Optional<Organization> findById(OrganizationId id);

    /**
     * Find an organization by name.
     */
    Optional<Organization> findByName(String name);

    /**
     * Find all organizations.
     */
    List<Organization> findAll();

    /**
     * Find all organizations for a user.
     */
    List<Organization> findAllByUserId(UserId userId);

    /**
     * Check if an organization name exists.
     */
    boolean existsByName(String name);

    /**
     * Save an organization.
     */
    Organization save(Organization organization);

    /**
     * Delete an organization (soft delete).
     */
    void delete(Organization organization);
}
