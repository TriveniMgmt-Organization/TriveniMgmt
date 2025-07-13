package com.store.mgmt.users.service;

import com.store.mgmt.users.model.dto.UserDTO;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserDTO request);
    UserDTO getUser(UUID id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(UUID id, UserDTO request);
    void deleteUser(UUID id);
    UserDTO assignRole(UUID userId, UUID roleId);
    UserDTO removeRole(UUID userId, UUID roleId);
}