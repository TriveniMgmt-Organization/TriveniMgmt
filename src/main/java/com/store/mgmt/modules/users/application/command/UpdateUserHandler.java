package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.domain.exception.UserNotFoundException;
import com.store.mgmt.modules.users.domain.model.*;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for UpdateUserCommand.
 */
@Component
@Transactional
public class UpdateUserHandler implements CommandHandler<UpdateUserCommand, UserDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserHandler.class);

    private final UserRepository userRepo;

    public UpdateUserHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDTO handle(UpdateUserCommand cmd) {
        log.debug("Updating user: {}", cmd.userId());

        User user = userRepo.findById(UserId.of(cmd.userId()))
                .orElseThrow(() -> new UserNotFoundException(UserId.of(cmd.userId())));

        // Update profile
        if (cmd.firstName() != null || cmd.lastName() != null) {
            PersonName newName = PersonName.of(
                    cmd.firstName() != null ? cmd.firstName() :
                            (user.getName() != null ? user.getName().firstName() : null),
                    cmd.lastName() != null ? cmd.lastName() :
                            (user.getName() != null ? user.getName().lastName() : null)
            );
            user.updateProfile(newName, cmd.imageUrl());
        } else if (cmd.imageUrl() != null) {
            user.updateProfile(null, cmd.imageUrl());
        }

        // Update active status
        if (cmd.active() != null) {
            if (cmd.active()) {
                user.activate();
            } else {
                user.deactivate();
            }
        }

        user = userRepo.save(user);

        log.info("Updated user: {}", cmd.userId());

        return toDTO(user);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().getValue())
                .username(user.getUsername().value())
                .email(user.getEmail().value())
                .firstName(user.getName() != null ? user.getName().firstName() : null)
                .lastName(user.getName() != null ? user.getName().lastName() : null)
                .imageUrl(user.getImageUrl())
                .active(user.isActive())
                .roles(List.of())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
