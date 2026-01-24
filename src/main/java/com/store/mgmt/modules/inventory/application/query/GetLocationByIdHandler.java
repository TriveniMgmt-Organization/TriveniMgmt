package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.modules.inventory.application.service.LocationMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetLocationByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetLocationByIdHandler implements QueryHandler<GetLocationByIdQuery, LocationResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetLocationByIdHandler.class);

    private final InventoryLocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public GetLocationByIdHandler(InventoryLocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public LocationResponseDTO handle(GetLocationByIdQuery query) {
        log.debug("Getting location by ID: {}", query.id());

        InventoryLocation location = locationRepository.findByIdAndStoreId(query.id(), query.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + query.id()));

        return locationMapper.toResponseDTO(location);
    }
}
