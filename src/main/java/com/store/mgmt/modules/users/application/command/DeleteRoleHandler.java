package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.domain.exception.RoleNotFoundException;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.RoleId;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

        Role role = roleRepo.findById(RoleId.of(cmd.roleId()))
                .orElseThrow(() -> new RoleNotFoundException(RoleId.of(cmd.roleId())));

        role.delete();
        roleRepo.delete(role);

        log.info("Deleted role: {}", cmd.roleId());

        return null;
    }
}
