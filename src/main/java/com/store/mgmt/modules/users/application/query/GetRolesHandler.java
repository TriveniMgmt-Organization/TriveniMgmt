package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetRolesQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetRolesHandler implements QueryHandler<GetRolesQuery, List<RoleDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetRolesHandler.class);

    private final RoleRepository roleRepo;

    public GetRolesHandler(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public List<RoleDTO> handle(GetRolesQuery query) {
        log.debug("Getting roles (page={}, size={})", query.page(), query.size());

        List<Role> roles = roleRepo.findAll(query.page(), query.size()).getContent();

        return roles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO toDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(List.of())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
