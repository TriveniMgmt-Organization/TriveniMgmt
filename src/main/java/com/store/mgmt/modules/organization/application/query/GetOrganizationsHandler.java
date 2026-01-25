package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler for GetOrganizationsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetOrganizationsHandler implements QueryHandler<GetOrganizationsQuery, List<OrganizationDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetOrganizationsHandler.class);

    private final OrganizationRepository orgRepo;
    private final StoreRepository storeRepo;

    public GetOrganizationsHandler(OrganizationRepository orgRepo, StoreRepository storeRepo) {
        this.orgRepo = orgRepo;
        this.storeRepo = storeRepo;
    }

    @Override
    public List<OrganizationDTO> handle(GetOrganizationsQuery query) {
        log.debug("Getting organizations for current user");

        TenantContext tenant = TenantContext.current();

        List<Organization> orgs = orgRepo.findAllByUserId(tenant.userId());

        // Simple pagination
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), orgs.size());

        if (start >= orgs.size()) {
            return List.of();
        }

        List<Organization> pagedOrgs = orgs.subList(start, end);

        // Load stores for all organizations in one query per org (could be optimized further)
        Map<UUID, List<Store>> storesByOrgId = pagedOrgs.stream()
                .collect(Collectors.toMap(
                        Organization::getId,
                        org -> storeRepo.findByOrganizationId(org.getId())
                ));

        return pagedOrgs.stream()
                .map(org -> toDTO(org, storesByOrgId.getOrDefault(org.getId(), List.of())))
                .collect(Collectors.toList());
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
