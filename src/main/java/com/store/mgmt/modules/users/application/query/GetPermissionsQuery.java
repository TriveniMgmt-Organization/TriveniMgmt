package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.PermissionDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all permissions.
 */
public record GetPermissionsQuery(
        int page,
        int size
) implements Query<List<PermissionDTO>> {}
