package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.modules.auth.application.dto.AuthUserDTO;
import com.store.mgmt.shared.application.query.Query;

/**
 * Query to get the currently authenticated user.
 */
public record GetCurrentUserQuery() implements Query<AuthUserDTO> {
}
