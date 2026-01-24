package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all organizations for the current user.
 */
public record GetOrganizationsQuery(
        int page,
        int size
) implements Query<List<OrganizationDTO>> {}
