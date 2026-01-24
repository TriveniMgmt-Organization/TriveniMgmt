package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetStoresQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetStoresHandler implements QueryHandler<GetStoresQuery, List<StoreDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetStoresHandler.class);

    private final StoreRepository storeRepo;

    public GetStoresHandler(StoreRepository storeRepo) {
        this.storeRepo = storeRepo;
    }

    @Override
    public List<StoreDTO> handle(GetStoresQuery query) {
        log.debug("Getting stores for organization: {}", query.organizationId());

        List<Store> stores = storeRepo.findByOrganizationId(query.organizationId());

        // Simple pagination
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), stores.size());

        if (start >= stores.size()) {
            return List.of();
        }

        return stores.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private StoreDTO toDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .organizationId(store.getOrganization().getId())
                .name(store.getName())
                .location(store.getLocation())
                .countryCode(store.getCountryCode())
                .contactInfo(store.getContactInfo())
                .status(store.getStatus().name())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
