package com.store.mgmt.modules.auth.application.query;

import com.store.mgmt.modules.organization.application.dto.OrganizationDTO;
import com.store.mgmt.modules.organization.application.dto.StoreDTO;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
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
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    public GetUserOrganizationsHandler(
            UserRepository userRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository
    ) {
        this.userRepository = userRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
    }

    @Override
    public List<OrganizationDTO> handle(GetUserOrganizationsQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Retrieving organizations for user: {}", username);

        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found."));

        // Fetch user's organization roles with related data
        List<UserOrganizationRole> userOrgRoles = userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(currentUser.getId());

        // Group stores by organization
        Map<Organization, List<Store>> orgStoresMap = userOrgRoles.stream()
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
