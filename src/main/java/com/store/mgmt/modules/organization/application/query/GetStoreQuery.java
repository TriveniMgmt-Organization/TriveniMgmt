package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.UUID;

/**
 * Query to get a store by ID.
 */
public record GetStoreQuery(
        UUID storeId
) implements Query<StoreDTO> {}
