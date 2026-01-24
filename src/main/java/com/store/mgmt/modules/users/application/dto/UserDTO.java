package com.store.mgmt.modules.users.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for User data.
 */
public class UserDTO {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private boolean active;
    private List<UserRoleAssignmentDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public List<UserRoleAssignmentDTO> getRoles() { return roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final UserDTO dto = new UserDTO();

        public Builder id(UUID id) { dto.id = id; return this; }
        public Builder username(String username) { dto.username = username; return this; }
        public Builder email(String email) { dto.email = email; return this; }
        public Builder firstName(String firstName) { dto.firstName = firstName; return this; }
        public Builder lastName(String lastName) { dto.lastName = lastName; return this; }
        public Builder imageUrl(String imageUrl) { dto.imageUrl = imageUrl; return this; }
        public Builder active(boolean active) { dto.active = active; return this; }
        public Builder roles(List<UserRoleAssignmentDTO> roles) { dto.roles = roles; return this; }
        public Builder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }

        public UserDTO build() { return dto; }
    }
}
