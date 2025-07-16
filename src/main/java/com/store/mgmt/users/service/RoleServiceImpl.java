package com.store.mgmt.users.service;

import com.store.mgmt.users.model.dto.PermissionDTO;
import com.store.mgmt.users.model.dto.RoleDTO;
import com.store.mgmt.users.model.entity.Permission;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.repository.PermissionRepository;
import com.store.mgmt.users.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO request) {
        if (request.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
        }
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name already exists");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        Role saved = roleRepository.save(role);
        return toDTO(saved);
    }

    @Override
    public RoleDTO getRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        return toDTO(role);
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO updateRole(UUID id, RoleDTO request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (request.getName() != null && !request.getName().equals(role.getName()) &&
                roleRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name already exists");
        }

        if (request.getName() != null) role.setName(request.getName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        Role updated = roleRepository.save(role);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDTO assignPermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));
        role.getPermissions().add(permission);
        Role updated = roleRepository.save(role);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public RoleDTO removePermission(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));

        if (!role.getPermissions().remove(permission)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission not assigned to this role");
        }

        Role updated = roleRepository.save(role);
        return toDTO(updated);
    }

    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setPermissions(role.getPermissions().stream().map(p -> {
            PermissionDTO permissionDTO = new PermissionDTO();
            permissionDTO.setId(p.getId());
            permissionDTO.setName(p.getName());
            permissionDTO.setDescription(p.getDescription());
            return permissionDTO;
        }).collect(Collectors.toSet()));
        return dto;
    }
}