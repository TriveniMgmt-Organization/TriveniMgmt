package com.store.mgmt.modules.users.infrastructure.web;

import com.store.mgmt.modules.users.application.command.*;
import com.store.mgmt.modules.users.application.dto.PermissionDTO;
import com.store.mgmt.modules.users.application.dto.RoleDTO;
import com.store.mgmt.modules.users.application.dto.UserDTO;
import com.store.mgmt.modules.users.application.query.*;
import com.store.mgmt.shared.infrastructure.CommandBus;
import com.store.mgmt.shared.infrastructure.QueryBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Users module using Clean Architecture.
 * Uses Command/Query buses to dispatch to handlers.
 */
@RestController
@RequestMapping("/api/v2/users")
@Tag(name = "Users Module (v2)", description = "Clean Architecture user management endpoints")
public class UsersModuleController {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public UsersModuleController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ==================== User Commands ====================

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        CreateUserCommand cmd = new CreateUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );

        UserDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UpdateUserCommand cmd = new UpdateUserCommand(
                userId,
                request.firstName(),
                request.lastName(),
                request.imageUrl(),
                request.active()
        );

        UserDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    @Operation(summary = "Delete user", description = "Soft delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        DeleteUserCommand cmd = new DeleteUserCommand(userId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @Operation(summary = "Assign role to user", description = "Assign a role to a user within an organization")
    public ResponseEntity<UserDTO> assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        AssignUserRoleCommand cmd = new AssignUserRoleCommand(
                userId,
                request.roleId(),
                request.organizationId(),
                request.storeId()
        );

        UserDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @Operation(summary = "Remove role from user", description = "Remove a role assignment from a user")
    public ResponseEntity<UserDTO> removeRole(
            @PathVariable UUID userId,
            @Valid @RequestBody RemoveRoleRequest request
    ) {
        RemoveUserRoleCommand cmd = new RemoveUserRoleCommand(
                userId,
                request.roleId(),
                request.organizationId(),
                request.storeId()
        );

        UserDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    // ==================== User Queries ====================

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(summary = "Get user", description = "Get a single user by ID")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID userId) {
        GetUserQuery query = new GetUserQuery(userId);
        UserDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    @Operation(summary = "List users", description = "Get all users with pagination")
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetUsersQuery query = new GetUsersQuery(page, size);
        List<UserDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Role Commands ====================

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    @Operation(summary = "Create role", description = "Create a new role")
    public ResponseEntity<RoleDTO> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        CreateRoleCommand cmd = new CreateRoleCommand(
                request.name(),
                request.description()
        );

        RoleDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @Operation(summary = "Delete role", description = "Soft delete a role")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        DeleteRoleCommand cmd = new DeleteRoleCommand(roleId);
        commandBus.dispatch(cmd);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    @Operation(summary = "Assign permission to role", description = "Add a permission to a role")
    public ResponseEntity<RoleDTO> assignPermissionToRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        AssignPermissionToRoleCommand cmd = new AssignPermissionToRoleCommand(roleId, permissionId);
        RoleDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a role")
    public ResponseEntity<RoleDTO> removePermissionFromRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        RemovePermissionFromRoleCommand cmd = new RemovePermissionFromRoleCommand(roleId, permissionId);
        RoleDTO result = commandBus.dispatch(cmd);
        return ResponseEntity.ok(result);
    }

    // ==================== Role/Permission Queries ====================

    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @Operation(summary = "Get role", description = "Get a single role by ID")
    public ResponseEntity<RoleDTO> getRole(@PathVariable UUID roleId) {
        GetRoleQuery query = new GetRoleQuery(roleId);
        RoleDTO result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @Operation(summary = "List roles", description = "Get all roles with pagination")
    public ResponseEntity<List<RoleDTO>> getRoles(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetRolesQuery query = new GetRolesQuery(page, size);
        List<RoleDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @Operation(summary = "List permissions", description = "Get all permissions with pagination")
    public ResponseEntity<List<PermissionDTO>> getPermissions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        GetPermissionsQuery query = new GetPermissionsQuery(page, size);
        List<PermissionDTO> result = queryBus.dispatch(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Request DTOs ====================

    public record CreateUserRequest(
            String username,
            String email,
            String password,
            String firstName,
            String lastName
    ) {}

    public record UpdateUserRequest(
            String firstName,
            String lastName,
            String imageUrl,
            Boolean active
    ) {}

    public record AssignRoleRequest(
            UUID roleId,
            UUID organizationId,
            UUID storeId
    ) {}

    public record RemoveRoleRequest(
            UUID roleId,
            UUID organizationId,
            UUID storeId
    ) {}

    public record CreateRoleRequest(
            String name,
            String description
    ) {}
}
