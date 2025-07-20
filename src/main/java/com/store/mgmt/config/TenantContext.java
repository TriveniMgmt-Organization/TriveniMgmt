package com.store.mgmt.config;

import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.store.mgmt.users.model.entity.User;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<Organization> organization = new ThreadLocal<>();
    private static final ThreadLocal<Store> store = new ThreadLocal<>();
    private static final ThreadLocal<User> user = new ThreadLocal<>();


    public static void setCurrentOrganization(Organization org) {
        organization.set(org);
    }

    public static Organization getCurrentOrganization() {
        return organization.get();
    }

    public static UUID getCurrentOrganizationId() {
        Organization org = organization.get();
        return org != null ? org.getId() : null;
    }

    public static void setCurrentStore(Store store) {
        TenantContext.store.set(store);
    }

    public static Store getCurrentStore() {
        return store.get();
    }

    public static UUID getCurrentStoreId() {
        Store storeEntity = store.get();
        return storeEntity != null ? storeEntity.getId() : null;
    }
    public static void setCurrentUser(User user) {
        TenantContext.user.set(user);
    }

    public static User getCurrentUser() {
        return user.get();
    }

    public static void clear() {
        organization.remove();
        store.remove();
        user.remove();
    }
}