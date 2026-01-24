package com.store.mgmt.config;

import com.store.mgmt.modules.organization.domain.model.StoreStatus;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.modules.organization.domain.repository.UserOrganizationRoleRepository;
import com.store.mgmt.modules.users.domain.model.Permission;
import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaPermissionRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaUserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final JpaUserRepository userRepository;
    private final JpaRoleRepository roleRepository;
    private final JpaPermissionRepository permissionRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(JpaUserRepository userRepository, JpaRoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            JpaPermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void seedData() {
        try {
            logger.info("Starting data seeding process...");
            seedPermissions();
            seedRoles();
            seedUsers();
            logger.info("Data seeding completed successfully.");
        } catch (Exception e) {
            logger.error("Data seeding failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to seed data: " + e.getMessage(), e);
        }
    }

    private void seedPermissions() {
        logger.debug("Seeding permissions...");
        Map<String, String> permissions = new HashMap<>();
        permissions.put("ORG_READ", "Permission to read org details");
        permissions.put("ORG_WRITE", "Permission to manage org");
        permissions.put("PRODUCT_READ", "Permission to read product details");
        permissions.put("PRODUCT_WRITE", "Permission to modify product details");
        permissions.put("USER_READ", "Permission to read user details");
        permissions.put("USER_WRITE", "Permission to modify user details");
        permissions.put("ROLE_READ", "Permission to read role details");
        permissions.put("ROLE_WRITE", "Permission to modify role details");
        permissions.put("INVENTORY_ITEM_READ", "Permission to read inventory items");
        permissions.put("INVENTORY_ITEM_WRITE", "Permission to modify inventory items");
        permissions.put("CATEGORY_READ", "Permission to read category details");
        permissions.put("CATEGORY_WRITE", "Permission to modify category details");
        permissions.put("BRAND_READ", "Permission to read brand details");
        permissions.put("BRAND_WRITE", "Permission to modify brand details");
        permissions.put("SUPPLIER_READ", "Permission to read supplier details");
        permissions.put("SUPPLIER_WRITE", "Permission to modify supplier details");
        permissions.put("LOCATION_READ", "Permission to read location details");
        permissions.put("LOCATION_WRITE", "Permission to modify location details");
        permissions.put("UOM_READ", "Permission to read unit of measurement details");
        permissions.put("UOM_WRITE", "Permission to modify unit of measurement details");
        permissions.put("PO_READ", "Permission to read purchase order details");
        permissions.put("PO_WRITE", "Permission to modify purchase order details");
        permissions.put("SALE_READ", "Permission to read sale details");
        permissions.put("SALE_WRITE", "Permission to modify sale details");
        permissions.put("DISCOUNT_READ", "Permission to read discount details");
        permissions.put("DISCOUNT_WRITE", "Permission to modify discount details");
        permissions.put("DAMAGE_LOSS_READ", "Permission to read damage and loss details");
        permissions.put("DAMAGE_LOSS_WRITE", "Permission to modify damage and loss details");
        permissions.put("STOCK_CHECK_READ", "Permission to read stock check details");
        permissions.put("REPORT_READ", "Permission to read reports");
        permissions.put("STORE_READ", "Permission to read store details");
        permissions.put("STORE_WRITE", "Permission to modify store details");

        long permissionCount = permissionRepository.count();
        List<Permission> newPermissions = new ArrayList<>();

        if (permissionCount == 0) {
            for (Map.Entry<String, String> entry : permissions.entrySet()) {
                Permission permission = new Permission();
                permission.setName(entry.getKey());
                permission.setDescription(entry.getValue());
                newPermissions.add(permission);
            }
        } else {
            Set<String> existingPermissionNames = permissionRepository.findAll()
                    .stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());

            for (Map.Entry<String, String> entry : permissions.entrySet()) {
                String permissionName = entry.getKey();
                if (!existingPermissionNames.contains(permissionName)) {
                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    permission.setDescription(entry.getValue());
                    newPermissions.add(permission);
                }
            }
        }

        if (!newPermissions.isEmpty()) {
            permissionRepository.saveAll(newPermissions);
            logger.info("Seeded {} new permissions", newPermissions.size());
        } else {
            logger.debug("No new permissions to seed");
        }
    }

    private void seedRoles() {
        logger.debug("Seeding roles...");

        Map<String, String> roles = getRolesMap();

        Map<String, List<String>> rolePermissions = new HashMap<>();
        rolePermissions.put("SUPER_ADMIN", Arrays.asList(
                "PRODUCT_READ", "PRODUCT_WRITE", "USER_READ", "USER_WRITE", "ROLE_READ", "ROLE_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "CATEGORY_READ", "CATEGORY_WRITE", "BRAND_READ",
                "BRAND_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ", "UOM_WRITE",
                "PO_READ", "PO_WRITE", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "DISCOUNT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ", "REPORT_READ", "ORG_READ", "ORG_WRITE",
                "STORE_READ", "STORE_WRITE"));
        rolePermissions.put("ORG_ADMIN", Arrays.asList(
                "USER_READ", "USER_WRITE", "ROLE_READ", "ROLE_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "CATEGORY_READ", "CATEGORY_WRITE", "BRAND_READ",
                "BRAND_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ", "UOM_WRITE",
                "PO_READ", "PO_WRITE", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "DISCOUNT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ", "ORG_READ", "ORG_WRITE", "STORE_READ",
                "STORE_WRITE"));
        rolePermissions.put("STORE_MANAGER", Arrays.asList(
                "USER_READ", "USER_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ",
                "PO_READ", "PO_WRITE", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "BRAND_READ", "CATEGORY_READ",
                "PRODUCT_READ", "PRODUCT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ", "STORE_WRITE", "STORE_READ"));
        rolePermissions.put("ADMIN", Arrays.asList(
                "PRODUCT_READ", "PRODUCT_WRITE", "USER_READ", "USER_WRITE", "ROLE_READ",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "CATEGORY_READ", "BRAND_READ",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ",
                "PO_READ", "PO_WRITE", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ", "REPORT_READ", "STORE_READ"));
        rolePermissions.put("MANAGER", Arrays.asList(
                "PRODUCT_READ", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "STOCK_CHECK_READ",
                "INVENTORY_ITEM_READ", "CATEGORY_READ", "BRAND_READ", "SUPPLIER_READ", "LOCATION_READ",
                "UOM_READ", "PO_READ", "SALE_READ", "DISCOUNT_READ", "DAMAGE_LOSS_READ", "STOCK_CHECK_READ",
                "STORE_READ"));
        rolePermissions.put("CASHIER", Arrays.asList(
                "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "STOCK_CHECK_READ", "STORE_READ"));
        rolePermissions.put("SUPPORT", Arrays.asList(
                "USER_READ", "USER_WRITE", "STORE_READ"));
        rolePermissions.put("CUSTOMER", Arrays.asList(
                "DISCOUNT_READ", "STOCK_CHECK_READ", "STORE_READ"));

        long roleCount = roleRepository.count();
        List<Role> rolesToSave = new ArrayList<>();

        Map<String, Permission> permissionMap = permissionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        if (roleCount == 0) {
            for (Map.Entry<String, String> entry : roles.entrySet()) {
                String roleName = entry.getKey();
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(entry.getValue());

                List<String> permissionNames = rolePermissions.getOrDefault(roleName, Collections.emptyList());
                Set<Permission> permissionSet = permissionNames.stream()
                        .map(name -> permissionMap.get(name))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                role.setPermissions(permissionSet);
                rolesToSave.add(role);
            }
        } else {
            logger.debug("Existing roles found, No need for seed...");
        }

        if (!rolesToSave.isEmpty()) {
            roleRepository.saveAll(rolesToSave);
            logger.info("Seeded/Updated {} roles", rolesToSave.size());
        } else {
            logger.debug("No new roles to seed");
        }
    }

    private static Map<String, String> getRolesMap() {
        Map<String, String> roles = new HashMap<>();
        roles.put("SUPER_ADMIN", "Role with all permissions and administrative access");
        roles.put("ORG_ADMIN", "Role with administrative access to manage organization settings and users");
        roles.put("STORE_MANAGER", "Role with permissions to manage store operations and inventory");
        roles.put("ADMIN", "Role with administrative access to manage users and resources");
        roles.put("MANAGER", "Role with permissions to manage sales and inventory");
        roles.put("CASHIER", "Role with permissions to handle sales transactions");
        roles.put("SUPPORT", "Role with permissions to assist users");
        roles.put("CUSTOMER", "Role with permissions to view discounts and stock availability");
        return roles;
    }

    private void seedUsers() {
        logger.debug("Seeding users...");

        String[][] users = {
                { "admin", "admin@store.com", "admin123", "SUPER_ADMIN" },
                { "manager", "manager@store.com", "manager123", "STORE_MANAGER" }
        };

        // Fetch all roles
        Map<String, Role> roleMap = roleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Role::getName, r -> r));
        Set<String> existingUserEmails = userRepository.findAll()
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());

        int usersCreated = 0;

        try {
            for (String[] userData : users) {
                String firstName = userData[0];
                String email = userData[1];
                String password = userData[2];
                String roleName = userData[3];

                if (existingUserEmails.contains(email)) {
                    logger.debug("User {} already exists, skipping.", email);
                    continue;
                }

                Role role = roleMap.get(roleName);
                if (role == null) {
                    logger.warn("Role {} not found, skipping user {}", roleName, email);
                    continue;
                }

                User user = new User();
                user.setFirstName(firstName);
                user.setLastName(firstName);
                user.setEmail(email);
                user.setUsername(email);
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setActive(true);
                user.setCreatedBy("system");
                user.setCreatedAt(LocalDateTime.now());

                String baseOrgName = firstName + "'s Organization";
                String orgName = baseOrgName;
                int suffix = 1;
                while (organizationRepository.findByName(orgName).isPresent()) {
                    orgName = baseOrgName + " " + suffix++;
                }

                Organization organization = new Organization();
                organization.setName(orgName);
                organization.setCreatedAt(LocalDateTime.now());
                organization.setCreatedBy("system");
                organization = organizationRepository.save(organization);

                Store defaultStore = new Store();
                defaultStore.setOrganization(organization);
                defaultStore.setName("Main Store");
                defaultStore.setLocation("Default Location");
                defaultStore.setStatus(StoreStatus.ACTIVE);
                defaultStore.setCreatedAt(LocalDateTime.now());
                defaultStore.setCreatedBy("system");
                storeRepository.save(defaultStore);
                logger.info("Created default store 'Main Store' for organization '{}'", organization.getName());

                // Save user first to get the ID
                user = userRepository.save(user);

                // Create UserOrganizationRole with UUID references
                UserOrganizationRole userOrgRole = new UserOrganizationRole();
                userOrgRole.setUserId(user.getId());
                userOrgRole.setOrganization(organization);
                userOrgRole.setRoleId(role.getId());
                userOrgRole.setCreatedAt(LocalDateTime.now());
                userOrgRole.setCreatedBy("system");
                userOrganizationRoleRepository.save(userOrgRole);

                logger.info("Seeded user {} with role {} in organization {}",
                        user.getEmail(), role.getName(), organization.getName());
                usersCreated++;
            }

            if (usersCreated > 0) {
                logger.info("Seeded {} new users.", usersCreated);
            } else {
                logger.debug("No new users to seed.");
            }

        } catch (Exception e) {
            logger.error("Data seeding failed: {}", e.getMessage(), e);
            throw new RuntimeException("Data seeding failed", e);
        }
    }
}
