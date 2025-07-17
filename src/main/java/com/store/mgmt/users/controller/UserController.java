package com.store.mgmt.users.controller;

import com.store.mgmt.users.model.dto.UserDTO;
import com.store.mgmt.users.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "Operations related to user management and roles")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account with the provided details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User created successfully",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'CREATE_USER' authority",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict: User with provided email/username already exists",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User details to be created", required = true)
            @RequestBody UserDTO request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a user by ID",
            description = "Retrieves a single user based on their unique identifier.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'VIEW_USER' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "Unique ID of the user to retrieve", required = true)
            @PathVariable UUID id) {
        UserDTO user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all available user accounts.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of users retrieved successfully",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'VIEW_USER' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing user",
            description = "Updates the details of an existing user identified by their ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User updated successfully",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or missing required fields",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'UPDATE_USER' authority",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict: Updated email/username already exists for another user",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "Unique ID of the user to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated user details", required = true)
            @RequestBody UserDTO request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user account based on their unique identifier. This operation is typically a hard delete.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "User deleted successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'DELETE_USER' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Unique ID of the user to delete", required = true)
            @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{roleId}")
    @Operation(
            summary = "Assign a role to a user",
            description = "Assigns a specific role to a user identified by their IDs.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Role assigned successfully",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User or Role not found",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'UPDATE_USER' authority",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict: Role already assigned to this user",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserDTO> assignRole(
            @Parameter(description = "Unique ID of the user", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Unique ID of the role to assign", required = true)
            @PathVariable UUID roleId) {
        UserDTO user = userService.assignRole(id, roleId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(
            summary = "Remove a role from a user",
            description = "Removes a specific role from a user identified by their IDs.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Role removed successfully",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User or Role not found, or role not assigned to user",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden: User does not have 'UPDATE_USER' authority",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<UserDTO> removeRole(
            @Parameter(description = "Unique ID of the user", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Unique ID of the role to remove", required = true)
            @PathVariable UUID roleId) {
        UserDTO user = userService.removeRole(id, roleId);
        return ResponseEntity.ok(user);
    }
}