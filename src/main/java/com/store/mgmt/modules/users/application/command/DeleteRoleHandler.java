package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.domain.exception.RoleNotFoundException;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteRoleCommand.
 */
@Component
@Transactional
public class DeleteRoleHandler implements CommandHandler<DeleteRoleCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteRoleHandler.class);

    private final RoleRepository roleRepo;

    public DeleteRoleHandler(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public Void handle(DeleteRoleCommand cmd) {
        log.debug("Deleting role: {}", cmd.roleId());

        Role role = roleRepo.findById(cmd.roleId())
                .orElseThrow(() -> new RoleNotFoundException(cmd.roleId()));

        // Soft delete
        role.setDeletedAt(LocalDateTime.now());
        roleRepo.save(role);

        log.info("Deleted role: {}", cmd.roleId());

        return null;
    }
}
