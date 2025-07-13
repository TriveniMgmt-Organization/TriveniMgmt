package com.store.mgmt.users.service;

import com.store.mgmt.users.model.dto.RoleDTO;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDTO createRole(RoleDTO request);
    RoleDTO getRole(UUID id);
    List<RoleDTO> getAllRoles();
    RoleDTO updateRole(UUID id, RoleDTO request);
    void deleteRole(UUID id);
    RoleDTO assignPermission(UUID roleId, UUID permissionId);
}