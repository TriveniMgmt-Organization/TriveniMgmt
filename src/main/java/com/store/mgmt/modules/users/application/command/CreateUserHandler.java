package com.store.mgmt.modules.users.application.command;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.domain.exception.UserAlreadyExistsException;
import com.store.mgmt.modules.users.domain.model.*;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for CreateUserCommand.
 */
@Component
@Transactional
public class CreateUserHandler implements CommandHandler<CreateUserCommand, UserDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateUserHandler.class);

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public CreateUserHandler(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO handle(CreateUserCommand cmd) {
        log.debug("Creating user: {}", cmd.username());

        Email email = Email.of(cmd.email());
        Username username = Username.of(cmd.username());

        // Check for duplicates
        if (userRepo.existsByEmail(email)) {
            throw new UserAlreadyExistsException("email", cmd.email());
        }
        if (userRepo.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", cmd.username());
        }

        // Create user
        User user = User.create(
                username,
                email,
                passwordEncoder.encode(cmd.password()),
                PersonName.of(cmd.firstName(), cmd.lastName())
        );

        user = userRepo.save(user);

        log.info("Created user: {}", user.getId().getValue());

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
