package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.application.dto.UoMConversionResponseDTO;
import com.store.mgmt.shared.application.query.Query;

import java.util.List;

/**
 * Query to get all UoM conversions.
 */
public record GetAllUoMConversionsQuery() implements Query<List<UoMConversionResponseDTO>> {}
