package com.store.mgmt.modules.organization.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.shared.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetOrganizationsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetOrganizationsHandler implements QueryHandler<GetOrganizationsQuery, List<OrganizationDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetOrganizationsHandler.class);

    private final OrganizationRepository orgRepo;

    public GetOrganizationsHandler(OrganizationRepository orgRepo) {
        this.orgRepo = orgRepo;
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

        return orgs.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private OrganizationDTO toDTO(Organization org) {
        return OrganizationDTO.builder()
                .id(org.getId())
                .name(org.getName())
                .description(org.getDescription())
                .contactInfo(org.getContactInfo())
                .appliedTemplateCode(org.getAppliedTemplateCode())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
