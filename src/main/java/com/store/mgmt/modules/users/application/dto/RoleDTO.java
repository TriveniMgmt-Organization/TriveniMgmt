package com.store.mgmt.modules.users.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Role data.
 */
public class RoleDTO {

    private UUID id;
    private String name;
    private String description;
    private List<PermissionDTO> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private RoleDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<PermissionDTO> getPermissions() { return permissions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final RoleDTO dto = new RoleDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder permissions(List<PermissionDTO> permissions) { dto.permissions = permissions; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }

        public RoleDTO build() { return dto; }
    }
}
