package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a user by ID.
 */
public record GetUserQuery(UUID userId) implements Query<UserDTO> {}
