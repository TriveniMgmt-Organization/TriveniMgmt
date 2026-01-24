package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.PermissionDTO;
import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.modules.users.domain.exception.RoleNotFoundException;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

        Role role = roleRepo.findById(query.roleId())
                .orElseThrow(() -> new RoleNotFoundException(query.roleId()));

        return toDTO(role);
    }

    private RoleDTO toDTO(Role role) {
        List<PermissionDTO> permissions = role.getPermissions() != null ?
                role.getPermissions().stream()
                        .map(p -> PermissionDTO.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .toList()
                : Collections.emptyList();

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
