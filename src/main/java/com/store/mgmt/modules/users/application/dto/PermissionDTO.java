package com.store.mgmt.modules.users.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Permission data.
 */
public class PermissionDTO {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PermissionDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final PermissionDTO dto = new PermissionDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }

        public PermissionDTO build() { return dto; }
    }
}
