package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.modules.users.domain.event.RoleCreated;
import com.store.mgmt.modules.users.domain.event.RolePermissionChanged;
import com.store.mgmt.shared.domain.model.AggregateRoot;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Role aggregate root - represents a role with permissions.
 */
public class Role extends AggregateRoot<RoleId> {

    private final RoleId id;
    private String name;
    private String description;
    private final Set<PermissionId> permissionIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Role(RoleId id) {
        this.id = id;
        this.permissionIds = new HashSet<>();
    }

    @Override
    public RoleId getId() {
        return id;
    }

    /**
     * Factory method to create a new role.
     */
    public static Role create(String name, String description) {
        Objects.requireNonNull(name, "Role name is required");

        Role role = new Role(RoleId.generate());
        role.name = name.trim();
        role.description = description;
        role.createdAt = LocalDateTime.now();
        role.updatedAt = role.createdAt;

        role.registerEvent(new RoleCreated(role.id, role.name));

        return role;
    }

    /**
     * Reconstitute from persistence.
     */
    public static Role reconstitute(
            RoleId id,
            String name,
            String description,
            Set<PermissionId> permissionIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        Role role = new Role(id);
        role.name = name;
        role.description = description;
        if (permissionIds != null) {
            role.permissionIds.addAll(permissionIds);
        }
        role.createdAt = createdAt;
        role.updatedAt = updatedAt;
        role.deletedAt = deletedAt;
        return role;
    }

    // ==================== Commands ====================

    public void update(String name, String description) {
        if (name != null && !name.equals(this.name)) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addPermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID is required");
        if (permissionIds.add(permissionId)) {
            this.updatedAt = LocalDateTime.now();
            registerEvent(new RolePermissionChanged(id, permissionId, true));
        }
    }

    public void removePermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission ID is required");
        if (permissionIds.remove(permissionId)) {
            this.updatedAt = LocalDateTime.now();
            registerEvent(new RolePermissionChanged(id, permissionId, false));
        }
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

    public boolean hasPermission(PermissionId permissionId) {
        return permissionIds.contains(permissionId);
    }

    public int getPermissionCount() {
        return permissionIds.size();
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<PermissionId> getPermissionIds() {
        return Collections.unmodifiableSet(permissionIds);
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
}
