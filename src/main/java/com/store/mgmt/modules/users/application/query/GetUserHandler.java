package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.application.dto.UserRoleAssignmentDTO;
import com.store.mgmt.modules.users.domain.exception.UserNotFoundException;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler for GetUserQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetUserHandler implements QueryHandler<GetUserQuery, UserDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetUserHandler.class);

    private final UserRepository userRepo;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final JpaRoleRepository roleRepository;

    public GetUserHandler(
            UserRepository userRepo,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            JpaRoleRepository roleRepository
    ) {
        this.userRepo = userRepo;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDTO handle(GetUserQuery query) {
        log.debug("Getting user: {}", query.userId());

        User user = userRepo.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));

        return toDTO(user);
    }

    private UserDTO toDTO(User user) {
        List<UserOrganizationRole> orgRoles = userOrganizationRoleRepository.findByUserIdWithOrganizationAndStore(user.getId());

        // Fetch all roles by IDs
        List<UUID> roleIds = orgRoles.stream()
                .map(UserOrganizationRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        Map<UUID, Role> roleMap = roleIds.isEmpty() ? Collections.emptyMap() :
                roleRepository.findByIdsWithPermissions(roleIds).stream()
                        .collect(Collectors.toMap(Role::getId, r -> r));

        List<UserRoleAssignmentDTO> roles = orgRoles.stream()
                .map(r -> {
                    Role role = roleMap.get(r.getRoleId());
                    return new UserRoleAssignmentDTO(
                            r.getRoleId(),
                            role != null ? role.getName() : null,
                            r.getOrganization().getId(),
                            r.getOrganization().getName(),
                            r.getStore() != null ? r.getStore().getId() : null,
                            r.getStore() != null ? r.getStore().getName() : null
                    );
                })
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .imageUrl(user.getImageUrl())
                .active(user.isActive())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
