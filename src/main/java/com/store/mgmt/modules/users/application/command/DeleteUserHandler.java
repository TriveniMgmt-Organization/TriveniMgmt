package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.domain.exception.UserNotFoundException;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handler for DeleteUserCommand.
 */
@Component
@Transactional
public class DeleteUserHandler implements CommandHandler<DeleteUserCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserHandler.class);

    private final UserRepository userRepo;

    public DeleteUserHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Void handle(DeleteUserCommand cmd) {
        log.debug("Deleting user: {}", cmd.userId());

        User user = userRepo.findById(cmd.userId())
                .orElseThrow(() -> new UserNotFoundException(cmd.userId()));

        // Soft delete
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        userRepo.save(user);

        log.info("Deleted user: {}", cmd.userId());

        return null;
    }
}
