package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for CreateRoleCommand.
 */
@Component
@Transactional
public class CreateRoleHandler implements CommandHandler<CreateRoleCommand, RoleDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateRoleHandler.class);

    private final RoleRepository roleRepo;

    public CreateRoleHandler(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public RoleDTO handle(CreateRoleCommand cmd) {
        log.debug("Creating role: {}", cmd.name());

        // Check for duplicate
        if (roleRepo.existsByName(cmd.name())) {
            throw new IllegalArgumentException("Role with name '" + cmd.name() + "' already exists");
        }

        // Create role
        Role role = new Role();
        role.setName(cmd.name());
        role.setDescription(cmd.description());

        role = roleRepo.save(role);

        log.info("Created role: {}", role.getId());

        return toDTO(role);
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
