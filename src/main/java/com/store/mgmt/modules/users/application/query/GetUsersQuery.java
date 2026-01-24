package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all users with pagination.
 */
public record GetUsersQuery(
        int page,
        int size
) implements Query<List<UserDTO>> {}
