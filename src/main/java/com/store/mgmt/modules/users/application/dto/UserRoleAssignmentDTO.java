package com.store.mgmt.modules.users.application.dto;

import java.util.UUID;

/**
 * DTO for a user's role assignment in an organization/store.
 */
public record UserRoleAssignmentDTO(
        UUID roleId,
        String roleName,
        UUID organizationId,
        String organizationName,
        UUID storeId,
        String storeName
) {}
