package com.store.mgmt.organization.service;

import com.store.mgmt.organization.model.dto.CreateOrganizationDTO;
import com.store.mgmt.organization.model.dto.OrganizationDTO;
import com.store.mgmt.organization.model.dto.UpdateOrganizationDTO;
import com.store.mgmt.organization.model.entity.Organization;

import java.util.UUID;

public interface OrganizationService {
    /**
     * Creates a new organization with the provided details.
     *
     * @param request the details of the organization to create
     * @return the created organization
     */
    OrganizationDTO createOrganization(CreateOrganizationDTO request);

    /**
     * Updates an existing organization with the provided details.
     *
     * @param request the updated details of the organization
     * @return the updated organization
     */
    OrganizationDTO updateOrganization(UUID id, UpdateOrganizationDTO request);

    /**
     * Deletes an organization by its ID.
     *
     * @param id the ID of the organization to delete
     */
    void deleteOrganization(UUID id);
}
