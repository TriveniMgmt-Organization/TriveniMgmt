package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.exception.StoreNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetStoreQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetStoreHandler implements QueryHandler<GetStoreQuery, StoreDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetStoreHandler.class);

    private final StoreRepository storeRepo;

    public GetStoreHandler(StoreRepository storeRepo) {
        this.storeRepo = storeRepo;
    }

    @Override
    public StoreDTO handle(GetStoreQuery query) {
        log.debug("Getting store: {}", query.storeId());

        Store store = storeRepo.findById(StoreId.of(query.storeId()))
                .orElseThrow(() -> new StoreNotFoundException(StoreId.of(query.storeId())));

        return toDTO(store);
    }

    private StoreDTO toDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId().getValue())
                .organizationId(store.getOrganizationId().getValue())
                .name(store.getName())
                .location(store.getLocation())
                .countryCode(store.getCountryCode())
                .contactInfo(store.getContactInfo() != null ? store.getContactInfo().getValue() : null)
                .status(store.getStatus().name())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
