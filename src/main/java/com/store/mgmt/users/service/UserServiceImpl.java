package com.store.mgmt.users.service;

import com.store.mgmt.users.model.dto.PermissionDTO;
import com.store.mgmt.users.model.dto.RoleDTO;
import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
import com.store.mgmt.users.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO request) {
        if (request.getUsername() == null || request.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and email are required");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFullName());
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    @Override
    public UserDTO getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername()) &&
                userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        if (request.getFullName() != null) user.setFirstName(request.getFullName());
        if (request.getEmail() != null){
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        }
        if (request.isActive() != user.isActive()) user.setActive(request.isActive());
        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        userRepository.save(user);
    }
    @Override
    @Transactional
    public UserDTO assignRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        user.getRoles().add(role);
        User updated = userRepository.save(user);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public UserDTO removeRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        user.getRoles().remove(role);
        User updated = userRepository.save(user);
        return toDTO(updated);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFirstName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setActive(user.isActive());
        dto.setRoles(
                user.getRoles().stream()
                        .map(role -> {
                            RoleDTO roleDTO = new RoleDTO();
                            roleDTO.setId(role.getId());
                            roleDTO.setName(role.getName());
                            roleDTO.setDescription(role.getDescription());
                            return roleDTO;
                        })
                        .collect(Collectors.toSet()));
        dto.setPermissions(
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> {
                            PermissionDTO permissionDTO = new PermissionDTO();
                            permissionDTO.setId(permission.getId());
                            permissionDTO.setName(permission.getName());
                            permissionDTO.setDescription(permission.getDescription());
                            return permissionDTO;
                        }) // Close the map function here
                        .collect(Collectors.toSet()) // Collect the stream into a Set
        );
        return dto;
    }
}