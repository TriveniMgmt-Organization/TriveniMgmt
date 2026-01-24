package com.store.mgmt.modules.users.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Permission entity - represents a system permission.
 */
public class Permission {

    private final PermissionId id;
    private String name;
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Permission(PermissionId id) {
        this.id = id;
    }

    public PermissionId getId() {
        return id;
    }

    /**
     * Factory method to create a new permission.
     */
    public static Permission create(String name, String description) {
        Objects.requireNonNull(name, "Permission name is required");

        Permission permission = new Permission(PermissionId.generate());
        permission.name = name.trim().toUpperCase();
        permission.description = description;
        permission.createdAt = LocalDateTime.now();
        permission.updatedAt = permission.createdAt;

        return permission;
    }

    /**
     * Reconstitute from persistence.
     */
    public static Permission reconstitute(
            PermissionId id,
            String name,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        Permission permission = new Permission(id);
        permission.name = name;
        permission.description = description;
        permission.createdAt = createdAt;
        permission.updatedAt = updatedAt;
        permission.deletedAt = deletedAt;
        return permission;
    }

    // ==================== Commands ====================

    public void update(String name, String description) {
        if (name != null) {
            this.name = name.trim().toUpperCase();
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.updatedAt = this.deletedAt;
        }
    }

    // ==================== Queries ====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
