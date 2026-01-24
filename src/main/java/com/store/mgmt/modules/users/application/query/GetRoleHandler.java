package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.PermissionDTO;
import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.modules.users.domain.exception.RoleNotFoundException;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.RoleId;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetRoleQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetRoleHandler implements QueryHandler<GetRoleQuery, RoleDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetRoleHandler.class);

    private final RoleRepository roleRepo;

    public GetRoleHandler(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public RoleDTO handle(GetRoleQuery query) {
        log.debug("Getting role: {}", query.roleId());

        Role role = roleRepo.findById(RoleId.of(query.roleId()))
                .orElseThrow(() -> new RoleNotFoundException(RoleId.of(query.roleId())));

        return toDTO(role);
    }

    private RoleDTO toDTO(Role role) {
        // Note: Permission details would need to be loaded from permission repository
        List<PermissionDTO> permissions = role.getPermissionIds().stream()
                .map(pid -> PermissionDTO.builder()
                        .id(pid.getValue())
                        .name(null) // Would need to be loaded
                        .build())
                .toList();

        return RoleDTO.builder()
                .id(role.getId().getValue())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
