package com.store.mgmt.users.controller;

import com.store.mgmt.users.model.dto.RoleDTO;
import com.store.mgmt.users.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO request) {
        RoleDTO role = roleService.createRole(request);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<RoleDTO> getRole(@PathVariable UUID id) {
        RoleDTO role = roleService.getRole(id);
        return ResponseEntity.ok(role);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable UUID id, @RequestBody RoleDTO request) {
        RoleDTO role = roleService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<RoleDTO> assignPermission(@PathVariable UUID id, @PathVariable UUID permissionId) {
        RoleDTO role = roleService.assignPermission(id, permissionId);
        return ResponseEntity.ok(role);
    }
}