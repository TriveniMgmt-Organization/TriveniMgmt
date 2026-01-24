package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.PermissionDTO;
import com.store.mgmt.modules.users.domain.model.Permission;
import com.store.mgmt.modules.users.domain.repository.PermissionRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetPermissionsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetPermissionsHandler implements QueryHandler<GetPermissionsQuery, List<PermissionDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetPermissionsHandler.class);

    private final PermissionRepository permissionRepo;

    public GetPermissionsHandler(PermissionRepository permissionRepo) {
        this.permissionRepo = permissionRepo;
    }

    @Override
    public List<PermissionDTO> handle(GetPermissionsQuery query) {
        log.debug("Getting permissions (page={}, size={})", query.page(), query.size());

        List<Permission> permissions = permissionRepo.findAll();

        // Simple pagination
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), permissions.size());

        if (start >= permissions.size()) {
            return List.of();
        }

        return permissions.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private PermissionDTO toDTO(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId().getValue())
                .name(permission.getName())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
