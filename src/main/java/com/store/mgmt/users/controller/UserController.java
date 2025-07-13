package com.store.mgmt.users.controller;

import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
        UserDTO user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USER')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @RequestBody UserDTO request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<UserDTO> assignRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        UserDTO user = userService.assignRole(id, roleId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<UserDTO> removeRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        UserDTO user = userService.removeRole(id, roleId);
        return ResponseEntity.ok(user);
    }
}