package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.application.dto.UserRoleAssignmentDTO;
import com.store.mgmt.modules.users.domain.exception.UserNotFoundException;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.model.UserId;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetUserQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetUserHandler implements QueryHandler<GetUserQuery, UserDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetUserHandler.class);

    private final UserRepository userRepo;

    public GetUserHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDTO handle(GetUserQuery query) {
        log.debug("Getting user: {}", query.userId());

        User user = userRepo.findById(UserId.of(query.userId()))
                .orElseThrow(() -> new UserNotFoundException(UserId.of(query.userId())));

        return toDTO(user);
    }

    private UserDTO toDTO(User user) {
        List<UserRoleAssignmentDTO> roles = user.getOrganizationRoles().stream()
                .map(r -> new UserRoleAssignmentDTO(
                        r.roleId().getValue(),
                        null, // Role name would need to be loaded
                        r.organizationId().getValue(),
                        null, // Org name would need to be loaded
                        r.storeId() != null ? r.storeId().getValue() : null,
                        null  // Store name would need to be loaded
                ))
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(user.getId().getValue())
                .username(user.getUsername().value())
                .email(user.getEmail().value())
                .firstName(user.getName() != null ? user.getName().firstName() : null)
                .lastName(user.getName() != null ? user.getName().lastName() : null)
                .imageUrl(user.getImageUrl())
                .active(user.isActive())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
