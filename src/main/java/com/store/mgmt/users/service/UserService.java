package com.store.mgmt.users.service;

import com.store.mgmt.users.model.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

/**
 * UserService interface defines the contract for user management operations.
 * It includes methods for creating, retrieving, updating, and deleting users,
 * as well as assigning and removing roles from users.
 */
public interface UserService  {
    UserDTO createUser(UserDTO request);
    UserDTO getUser(UUID id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(UUID id, UserDTO request);
    void deleteUser(UUID id);
    UserDTO assignRole(UUID userId, UUID roleId);
    UserDTO removeRole(UUID userId, UUID roleId);
}