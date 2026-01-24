package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get an organization by ID.
 */
public record GetOrganizationQuery(
        UUID organizationId
) implements Query<OrganizationDTO> {}
