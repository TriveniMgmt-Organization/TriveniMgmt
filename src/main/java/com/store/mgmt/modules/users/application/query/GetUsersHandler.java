package com.store.mgmt.modules.users.application.query;

import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetUsersQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetUsersHandler implements QueryHandler<GetUsersQuery, List<UserDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetUsersHandler.class);

    private final UserRepository userRepo;

    public GetUsersHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public List<UserDTO> handle(GetUsersQuery query) {
        log.debug("Getting users (page={}, size={})", query.page(), query.size());

        List<User> users = userRepo.findAll();

        // Simple pagination
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), users.size());

        if (start >= users.size()) {
            return List.of();
        }

        return users.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
