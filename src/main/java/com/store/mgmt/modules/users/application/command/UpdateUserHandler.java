package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.domain.exception.UserNotFoundException;
import com.store.mgmt.modules.users.domain.model.User;
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

        User user = userRepo.findById(cmd.userId())
                .orElseThrow(() -> new UserNotFoundException(cmd.userId()));

        // Update profile
        if (cmd.firstName() != null) {
            user.setFirstName(cmd.firstName());
        }
        if (cmd.lastName() != null) {
            user.setLastName(cmd.lastName());
        }
        if (cmd.imageUrl() != null) {
            user.setImageUrl(cmd.imageUrl());
        }

        // Update active status
        if (cmd.active() != null) {
            user.setActive(cmd.active());
        }

        user = userRepo.save(user);

        log.info("Updated user: {}", cmd.userId());

        return toDTO(user);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .imageUrl(user.getImageUrl())
                .active(user.isActive())
                .roles(List.of())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
