package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.shared.application.query.Query;

/**
 * Query to validate if the current access token is valid.
 */
public record ValidateTokenQuery(
        String accessToken
) implements Query<Boolean> {
}
