package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.InventoryLocation;
import com.store.mgmt.modules.inventory.domain.repository.InventoryLocationRepository;
import com.store.mgmt.modules.inventory.application.dto.LocationResponseDTO;
import com.store.mgmt.modules.inventory.application.service.LocationMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllLocationsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllLocationsHandler implements QueryHandler<GetAllLocationsQuery, List<LocationResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllLocationsHandler.class);

    private final InventoryLocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public GetAllLocationsHandler(InventoryLocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public List<LocationResponseDTO> handle(GetAllLocationsQuery query) {
        log.debug("Getting all locations for store: {}, includeInactive: {}",
                query.storeId(), query.includeInactive());

        List<InventoryLocation> locations = locationRepository.findByStoreId(query.storeId())
                .stream()
                .filter(l -> l.getDeletedAt() == null)
                .toList();

        if (!query.includeInactive()) {
            locations = locations.stream()
                    .filter(InventoryLocation::isActive)
                    .toList();
        }

        return locationMapper.toResponseDTOList(locations);
    }
}
