package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;
import java.util.UUID;

/**
 * Query to get all stores for an organization.
 */
public record GetStoresQuery(
        UUID organizationId,
        int page,
        int size
) implements Query<List<StoreDTO>> {}
