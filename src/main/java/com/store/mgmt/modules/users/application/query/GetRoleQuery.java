package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a role by ID.
 */
public record GetRoleQuery(UUID roleId) implements Query<RoleDTO> {}
