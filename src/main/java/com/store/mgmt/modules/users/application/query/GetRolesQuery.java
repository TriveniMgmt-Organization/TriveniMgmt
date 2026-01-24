package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all roles with pagination.
 */
public record GetRolesQuery(
        int page,
        int size
) implements Query<List<RoleDTO>> {}
