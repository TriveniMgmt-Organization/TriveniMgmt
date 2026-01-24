package com.store.mgmt.modules.users.domain.model;

import com.store.mgmt.modules.organization.domain.model.OrganizationId;
import com.store.mgmt.modules.organization.domain.model.StoreId;
import com.store.mgmt.modules.users.domain.event.UserCreated;
import com.store.mgmt.modules.users.domain.event.UserDeactivated;
import com.store.mgmt.modules.users.domain.event.UserRoleAssigned;
import com.store.mgmt.modules.users.domain.event.UserRoleRemoved;
import com.store.mgmt.shared.domain.model.AggregateRoot;

import java.time.LocalDateTime;
import java.util.*;

/**
 * User aggregate root - represents a system user.
 */
public class User extends AggregateRoot<UserId> {

    private final UserId id;
    private Username username;
    private Email email;
    private String passwordHash;
    private PersonName name;
    private String imageUrl;
    private boolean active;
    private final Set<UserOrganizationRole> organizationRoles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private User(UserId id) {
        this.id = id;
        this.organizationRoles = new HashSet<>();
        this.active = true;
    }

    @Override
    public UserId getId() {
        return id;
    }

    /**
     * Factory method to create a new user.
     */
    public static User create(
            Username username,
            Email email,
            String passwordHash,
            PersonName name
    ) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(passwordHash, "Password hash is required");

        User user = new User(UserId.generate());
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.name = name;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = user.createdAt;

        user.registerEvent(new UserCreated(user.id, user.username.value(), user.email.value()));

        return user;
    }

    /**
     * Reconstitute from persistence.
     */
    public static User reconstitute(
            UserId id,
            Username username,
            Email email,
            String passwordHash,
            PersonName name,
            String imageUrl,
            boolean active,
            Set<UserOrganizationRole> organizationRoles,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        User user = new User(id);
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.name = name;
        user.imageUrl = imageUrl;
        user.active = active;
        if (organizationRoles != null) {
            user.organizationRoles.addAll(organizationRoles);
        }
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        user.deletedAt = deletedAt;
        return user;
    }

    // ==================== Commands ====================

    public void updateProfile(PersonName name, String imageUrl) {
        if (name != null) {
            this.name = name;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmail(Email newEmail) {
        Objects.requireNonNull(newEmail, "Email is required");
        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPasswordHash) {
        Objects.requireNonNull(newPasswordHash, "Password hash is required");
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignRole(RoleId roleId, OrganizationId organizationId, StoreId storeId) {
        Objects.requireNonNull(roleId, "Role ID is required");
        Objects.requireNonNull(organizationId, "Organization ID is required");

        UserOrganizationRole role = new UserOrganizationRole(roleId, organizationId, storeId);
        if (organizationRoles.add(role)) {
            this.updatedAt = LocalDateTime.now();
            registerEvent(new UserRoleAssigned(id, roleId, organizationId, storeId));
        }
    }

    public void removeRole(RoleId roleId, OrganizationId organizationId, StoreId storeId) {
        UserOrganizationRole role = new UserOrganizationRole(roleId, organizationId, storeId);
        if (organizationRoles.remove(role)) {
            this.updatedAt = LocalDateTime.now();
            registerEvent(new UserRoleRemoved(id, roleId, organizationId, storeId));
        }
    }

    public void activate() {
        if (!this.active) {
            this.active = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void deactivate() {
        if (this.active) {
            this.active = false;
            this.updatedAt = LocalDateTime.now();
            registerEvent(new UserDeactivated(id));
        }
    }

    public void delete() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
            this.updatedAt = this.deletedAt;
            this.active = false;
        }
    }

    // ==================== Queries ====================

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasRoleInOrganization(OrganizationId organizationId) {
        return organizationRoles.stream()
                .anyMatch(r -> r.organizationId().equals(organizationId));
    }

    public boolean hasRoleInStore(StoreId storeId) {
        return organizationRoles.stream()
                .anyMatch(r -> storeId.equals(r.storeId()));
    }

    public Set<OrganizationId> getOrganizationIds() {
        Set<OrganizationId> orgIds = new HashSet<>();
        organizationRoles.forEach(r -> orgIds.add(r.organizationId()));
        return Collections.unmodifiableSet(orgIds);
    }

    // ==================== Getters ====================

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public PersonName getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Set<UserOrganizationRole> getOrganizationRoles() {
        return Collections.unmodifiableSet(organizationRoles);
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
