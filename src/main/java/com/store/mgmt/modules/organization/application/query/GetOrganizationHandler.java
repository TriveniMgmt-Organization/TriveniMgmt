package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.exception.OrganizationNotFoundException;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetOrganizationQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetOrganizationHandler implements QueryHandler<GetOrganizationQuery, OrganizationDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetOrganizationHandler.class);

    private final OrganizationRepository orgRepo;
    private final StoreRepository storeRepo;

    public GetOrganizationHandler(OrganizationRepository orgRepo, StoreRepository storeRepo) {
        this.orgRepo = orgRepo;
        this.storeRepo = storeRepo;
    }

    @Override
    public OrganizationDTO handle(GetOrganizationQuery query) {
        log.debug("Getting organization: {}", query.organizationId());

        Organization org = orgRepo.findById(query.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(query.organizationId()));

        // Load stores
        List<Store> stores = storeRepo.findByOrganizationId(org.getId());

        return toDTO(org, stores);
    }

    private OrganizationDTO toDTO(Organization org, List<Store> stores) {
        List<StoreDTO> storeDTOs = stores.stream()
                .map(this::toStoreDTO)
                .collect(Collectors.toList());

        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .description(org.getDescription())
                .contactInfo(org.getContactInfo())
                .appliedTemplateCode(org.getAppliedTemplateCode())
                .stores(storeDTOs)
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    private StoreDTO toStoreDTO(Store store) {
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
