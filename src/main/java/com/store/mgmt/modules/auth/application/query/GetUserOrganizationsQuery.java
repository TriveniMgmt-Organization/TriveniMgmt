package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all organizations the current user belongs to.
 */
public record GetUserOrganizationsQuery() implements Query<List<OrganizationDTO>> {
}
