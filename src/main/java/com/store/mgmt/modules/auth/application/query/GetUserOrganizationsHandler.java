package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handler for GetUserOrganizationsQuery.
 * Returns all organizations the current user belongs to.
 */
@Component
@Transactional(readOnly = true)
public class GetUserOrganizationsHandler implements QueryHandler<GetUserOrganizationsQuery, List<OrganizationDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetUserOrganizationsHandler.class);

    private final UserRepository userRepository;

    public GetUserOrganizationsHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<OrganizationDTO> handle(GetUserOrganizationsQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Retrieving organizations for user: {}", username);

        // Use optimized query to fetch user with all related data
        User currentUser = userRepository.findByUsernameWithAllRelatedData(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found."));

        // Group stores by organization
        Map<Organization, List<Store>> orgStoresMap = currentUser.getOrganizationRoles().stream()
                .collect(Collectors.groupingBy(
                        UserOrganizationRole::getOrganization,
                        Collectors.mapping(UserOrganizationRole::getStore, Collectors.toList())
                ));

        return orgStoresMap.entrySet().stream()
                .map(entry -> {
                    Organization org = entry.getKey();
                    OrganizationDTO orgDto = OrganizationDTO.builder()
                            .id(org.getId())
                            .name(org.getName())
                            .description(org.getDescription())
                            .contactInfo(org.getContactInfo())
                            .appliedTemplateCode(org.getAppliedTemplateCode())
                            .createdAt(org.getCreatedAt())
                            .updatedAt(org.getUpdatedAt())
                            .build();

                    List<StoreDTO> storeDtos = entry.getValue().stream()
                            .filter(Objects::nonNull)
                            .distinct()
                            .map(store -> StoreDTO.builder()
                                    .id(store.getId())
                                    .name(store.getName())
                                    .location(store.getLocation())
                                    .contactInfo(store.getContactInfo())
                                    .status(store.getStatus() != null ? store.getStatus().name() : null)
                                    .build())
                            .collect(Collectors.toList());
                    orgDto.setStores(storeDtos);

                    return orgDto;
                })
                .collect(Collectors.toList());
    }
}
