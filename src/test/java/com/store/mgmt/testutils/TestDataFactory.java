package com.store.mgmt.testutils;

import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.users.domain.model.Permission;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Factory class for creating test data objects.
 */
public class TestDataFactory {

    public static User createUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash("encoded-password");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("test");
        return user;
    }

    public static Organization createOrganization(String name) {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName(name);
        org.setDescription("Test organization");
        org.setContactInfo("test@org.com");
        org.setCreatedAt(LocalDateTime.now());
        org.setCreatedBy("test");
        org.setStores(new HashSet<>());
        return org;
    }

    public static Store createStore(String name, Organization organization) {
        Store store = new Store();
        store.setId(UUID.randomUUID());
        store.setName(name);
        store.setLocation("Test Location");
        store.setContactInfo("store@test.com");
        store.setStatus(StoreStatus.ACTIVE);
        store.setOrganization(organization);
        store.setCreatedAt(LocalDateTime.now());
        store.setCreatedBy("test");

        if (organization.getStores() == null) {
            organization.setStores(new HashSet<>());
        }
        organization.getStores().add(store);

        return store;
    }

    public static Role createRole(String name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        role.setDescription("Test role");
        role.setPermissions(new HashSet<>());
        role.setCreatedAt(LocalDateTime.now());
        role.setCreatedBy("test");
        return role;
    }

    public static Permission createPermission(String name) {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setName(name);
        permission.setDescription("Test permission");
        permission.setCreatedAt(LocalDateTime.now());
        permission.setCreatedBy("test");
        return permission;
    }

    public static Role createRoleWithPermissions(String name, String... permissionNames) {
        Role role = createRole(name);
        Set<Permission> permissions = new HashSet<>();
        for (String permName : permissionNames) {
            permissions.add(createPermission(permName));
        }
        role.setPermissions(permissions);
        return role;
    }

    /**
     * Creates an organization-level role assignment (store is null).
     * This grants access to all stores in the organization.
     */
    public static UserOrganizationRole createOrgLevelRole(User user, Organization organization, Role role) {
        UserOrganizationRole uor = new UserOrganizationRole();
        uor.setId(UUID.randomUUID());
        uor.setUserId(user.getId());
        uor.setOrganization(organization);
        uor.setRoleId(role.getId());
        uor.setStore(null); // Org-level role
        uor.setCreatedAt(LocalDateTime.now());
        uor.setCreatedBy("test");
        return uor;
    }

    /**
     * Creates a store-level role assignment.
     * This grants access only to the specific store.
     */
    public static UserOrganizationRole createStoreLevelRole(User user, Organization organization, Store store, Role role) {
        UserOrganizationRole uor = new UserOrganizationRole();
        uor.setId(UUID.randomUUID());
        uor.setUserId(user.getId());
        uor.setOrganization(organization);
        uor.setRoleId(role.getId());
        uor.setStore(store); // Store-level role
        uor.setCreatedAt(LocalDateTime.now());
        uor.setCreatedBy("test");
        return uor;
    }
}
