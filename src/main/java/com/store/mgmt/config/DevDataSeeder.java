package com.store.mgmt.config;

import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.organization.domain.model.StoreStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Development-only data seeder.
 * Seeds permissions, roles, role-permission mappings, and demo users.
 * Only runs when the 'dev' profile is active.
 *
 * In production, Liquibase handles permissions/roles seeding.
 */
@Component
@Profile("dev")
@Order(1)
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevDataSeeder.class);

    private final JpaUserRepository userRepository;
    private final JpaRoleRepository roleRepository;
    private final JpaPermissionRepository permissionRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalTemplateSeeder globalTemplateSeeder;

    public DevDataSeeder(
            JpaUserRepository userRepository,
            JpaRoleRepository roleRepository,
            JpaPermissionRepository permissionRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            PasswordEncoder passwordEncoder,
            @Lazy GlobalTemplateSeeder globalTemplateSeeder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.globalTemplateSeeder = globalTemplateSeeder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("DevDataSeeder: Starting development data seeding...");

        try {
            seedPermissions();
            seedRoles();
            seedDemoUsers();
            seedGlobalTemplates();
            logger.info("DevDataSeeder: Development data seeding completed successfully.");
        } catch (Exception e) {
            logger.error("DevDataSeeder: Failed to seed development data: {}", e.getMessage(), e);
            logger.warn("DevDataSeeder: Application will continue despite seeding failure.");
        }
    }

    private void seedGlobalTemplates() {
        logger.info("DevDataSeeder: Seeding global templates...");
        int count = globalTemplateSeeder.seedGlobalTemplates();
        if (count > 0) {
            logger.info("DevDataSeeder: Seeded {} global templates.", count);
        } else {
            logger.info("DevDataSeeder: All global templates already exist.");
        }
    }

    private void seedPermissions() {
        logger.info("DevDataSeeder: Seeding permissions...");

        Map<String, String> permissions = new LinkedHashMap<>();
        // Organization
        permissions.put("ORG_READ", "Permission to read organization details");
        permissions.put("ORG_WRITE", "Permission to manage organization settings");
        // Store
        permissions.put("STORE_READ", "Permission to read store details");
        permissions.put("STORE_WRITE", "Permission to modify store settings");
        // Products
        permissions.put("PRODUCT_READ", "Permission to read product details");
        permissions.put("PRODUCT_WRITE", "Permission to modify product details");
        permissions.put("COST_READ", "Permission to view product cost information");
        permissions.put("MARGIN_READ", "Permission to view profit margin information");
        // Users
        permissions.put("USER_READ", "Permission to read user details");
        permissions.put("USER_WRITE", "Permission to modify user details");
        permissions.put("USER_INVITE", "Permission to invite new users");
        permissions.put("ROLE_READ", "Permission to read role details");
        permissions.put("ROLE_WRITE", "Permission to modify role assignments");
        // Inventory
        permissions.put("INVENTORY_ITEM_READ", "Permission to read inventory items");
        permissions.put("INVENTORY_ITEM_WRITE", "Permission to modify inventory items");
        permissions.put("STOCK_ADJUST", "Permission to adjust stock quantities");
        permissions.put("CYCLE_COUNT", "Permission to perform inventory cycle counts");
        permissions.put("TRANSFER_CREATE", "Permission to create stock transfers");
        permissions.put("TRANSFER_APPROVE", "Permission to approve stock transfers");
        // Purchase Orders
        permissions.put("PO_READ", "Permission to read purchase orders");
        permissions.put("PO_WRITE", "Permission to create/modify purchase orders");
        permissions.put("PO_APPROVE", "Permission to approve purchase orders");
        permissions.put("RECEIVING_CREATE", "Permission to receive goods against POs");
        // Sales
        permissions.put("SALE_READ", "Permission to read sales data");
        permissions.put("SALE_WRITE", "Permission to create/modify sales");
        permissions.put("VOID_SALE", "Permission to void sales transactions");
        permissions.put("REFUND_APPROVE", "Permission to approve refunds");
        permissions.put("PRICE_OVERRIDE", "Permission to override item prices");
        permissions.put("DISCOUNT_APPLY", "Permission to apply discounts at POS");
        // Cash
        permissions.put("CASH_DRAWER_READ", "Permission to view cash drawer status");
        permissions.put("CASH_DRAWER_WRITE", "Permission to manage cash drawer");
        permissions.put("END_OF_DAY", "Permission to perform end-of-day reconciliation");
        // Reports
        permissions.put("REPORT_READ", "Permission to view basic reports");
        permissions.put("REPORT_FINANCIAL", "Permission to view financial reports");
        permissions.put("REPORT_INVENTORY", "Permission to view inventory reports");
        // Reference Data
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
        // Other
        permissions.put("DISCOUNT_READ", "Permission to read discount configurations");
        permissions.put("DISCOUNT_WRITE", "Permission to modify discount configurations");
        permissions.put("DAMAGE_LOSS_READ", "Permission to read damage and loss records");
        permissions.put("DAMAGE_LOSS_WRITE", "Permission to record damage and loss");
        permissions.put("STOCK_CHECK_READ", "Permission to read stock check details");
        // Templates
        permissions.put("TEMPLATE_READ", "Permission to view global templates");
        permissions.put("TEMPLATE_WRITE", "Permission to manage and apply templates");

        Set<String> existingPermissions = permissionRepository.findAll().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Permission> newPermissions = new ArrayList<>();
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            if (!existingPermissions.contains(entry.getKey())) {
                Permission permission = new Permission();
                permission.setName(entry.getKey());
                permission.setDescription(entry.getValue());
                newPermissions.add(permission);
            }
        }

        if (!newPermissions.isEmpty()) {
            permissionRepository.saveAll(newPermissions);
            logger.info("DevDataSeeder: Seeded {} permissions.", newPermissions.size());
        } else {
            logger.info("DevDataSeeder: All {} permissions already exist.", permissions.size());
        }
    }

    private void seedRoles() {
        logger.info("DevDataSeeder: Seeding roles...");

        Map<String, String> roles = new LinkedHashMap<>();
        roles.put("SUPER_ADMIN", "Platform administrator with full system access");
        roles.put("ORG_ADMIN", "Organization owner with full access to organization settings");
        roles.put("ACCOUNTANT", "Financial reporting and accounting access");
        roles.put("STORE_MANAGER", "Full store management access");
        roles.put("SHIFT_LEAD", "Shift supervisor with limited management capabilities");
        roles.put("CASHIER", "Point-of-sale operations");
        roles.put("INVENTORY_SPECIALIST", "Stock management and inventory operations");
        roles.put("PURCHASING_AGENT", "Vendor and purchase order management");
        roles.put("STAFF", "General read access for basic operations");

        Map<String, List<String>> rolePermissions = new LinkedHashMap<>();
        // SUPER_ADMIN - all permissions
        rolePermissions.put("SUPER_ADMIN", Arrays.asList(
                "ORG_READ", "ORG_WRITE", "STORE_READ", "STORE_WRITE",
                "PRODUCT_READ", "PRODUCT_WRITE", "COST_READ", "MARGIN_READ",
                "USER_READ", "USER_WRITE", "USER_INVITE", "ROLE_READ", "ROLE_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "STOCK_ADJUST", "CYCLE_COUNT", "TRANSFER_CREATE", "TRANSFER_APPROVE",
                "PO_READ", "PO_WRITE", "PO_APPROVE", "RECEIVING_CREATE",
                "SALE_READ", "SALE_WRITE", "VOID_SALE", "REFUND_APPROVE", "PRICE_OVERRIDE", "DISCOUNT_APPLY",
                "CASH_DRAWER_READ", "CASH_DRAWER_WRITE", "END_OF_DAY",
                "REPORT_READ", "REPORT_FINANCIAL", "REPORT_INVENTORY",
                "CATEGORY_READ", "CATEGORY_WRITE", "BRAND_READ", "BRAND_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE",
                "UOM_READ", "UOM_WRITE", "DISCOUNT_READ", "DISCOUNT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ",
                "TEMPLATE_READ", "TEMPLATE_WRITE"
        ));
        // ORG_ADMIN - same as SUPER_ADMIN for organization scope
        rolePermissions.put("ORG_ADMIN", rolePermissions.get("SUPER_ADMIN"));
        // STORE_MANAGER
        rolePermissions.put("STORE_MANAGER", Arrays.asList(
                "STORE_READ", "STORE_WRITE",
                "PRODUCT_READ", "PRODUCT_WRITE", "COST_READ", "MARGIN_READ",
                "USER_READ", "USER_WRITE", "USER_INVITE", "ROLE_READ",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "STOCK_ADJUST", "CYCLE_COUNT", "TRANSFER_CREATE", "TRANSFER_APPROVE",
                "PO_READ", "PO_WRITE", "PO_APPROVE", "RECEIVING_CREATE",
                "SALE_READ", "SALE_WRITE", "VOID_SALE", "REFUND_APPROVE", "PRICE_OVERRIDE", "DISCOUNT_APPLY",
                "CASH_DRAWER_READ", "CASH_DRAWER_WRITE", "END_OF_DAY",
                "REPORT_READ", "REPORT_FINANCIAL", "REPORT_INVENTORY",
                "CATEGORY_READ", "CATEGORY_WRITE", "BRAND_READ", "BRAND_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE",
                "UOM_READ", "UOM_WRITE", "DISCOUNT_READ", "DISCOUNT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ"
        ));
        // SHIFT_LEAD
        rolePermissions.put("SHIFT_LEAD", Arrays.asList(
                "STORE_READ", "PRODUCT_READ", "COST_READ", "USER_READ",
                "INVENTORY_ITEM_READ", "STOCK_ADJUST", "CYCLE_COUNT",
                "PO_READ", "RECEIVING_CREATE",
                "SALE_READ", "SALE_WRITE", "VOID_SALE", "PRICE_OVERRIDE", "DISCOUNT_APPLY",
                "CASH_DRAWER_READ", "CASH_DRAWER_WRITE", "END_OF_DAY",
                "REPORT_READ",
                "CATEGORY_READ", "BRAND_READ", "SUPPLIER_READ", "LOCATION_READ", "UOM_READ",
                "DISCOUNT_READ", "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ"
        ));
        // CASHIER
        rolePermissions.put("CASHIER", Arrays.asList(
                "STORE_READ", "PRODUCT_READ", "INVENTORY_ITEM_READ",
                "SALE_READ", "SALE_WRITE", "DISCOUNT_APPLY",
                "CASH_DRAWER_READ",
                "CATEGORY_READ", "BRAND_READ", "UOM_READ",
                "DISCOUNT_READ", "STOCK_CHECK_READ"
        ));
        // INVENTORY_SPECIALIST
        rolePermissions.put("INVENTORY_SPECIALIST", Arrays.asList(
                "STORE_READ", "PRODUCT_READ", "PRODUCT_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "STOCK_ADJUST", "CYCLE_COUNT", "TRANSFER_CREATE",
                "PO_READ", "RECEIVING_CREATE",
                "REPORT_INVENTORY",
                "CATEGORY_READ", "BRAND_READ", "SUPPLIER_READ", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ"
        ));
        // PURCHASING_AGENT
        rolePermissions.put("PURCHASING_AGENT", Arrays.asList(
                "STORE_READ", "PRODUCT_READ", "COST_READ", "INVENTORY_ITEM_READ",
                "PO_READ", "PO_WRITE", "RECEIVING_CREATE",
                "REPORT_INVENTORY",
                "CATEGORY_READ", "BRAND_READ", "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "UOM_READ",
                "STOCK_CHECK_READ"
        ));
        // ACCOUNTANT
        rolePermissions.put("ACCOUNTANT", Arrays.asList(
                "ORG_READ", "STORE_READ",
                "PRODUCT_READ", "COST_READ", "MARGIN_READ",
                "INVENTORY_ITEM_READ", "PO_READ", "SALE_READ", "CASH_DRAWER_READ",
                "REPORT_READ", "REPORT_FINANCIAL", "REPORT_INVENTORY",
                "CATEGORY_READ", "BRAND_READ", "SUPPLIER_READ", "LOCATION_READ", "UOM_READ",
                "DISCOUNT_READ", "DAMAGE_LOSS_READ", "STOCK_CHECK_READ"
        ));
        // STAFF
        rolePermissions.put("STAFF", Arrays.asList(
                "STORE_READ", "PRODUCT_READ", "INVENTORY_ITEM_READ",
                "CATEGORY_READ", "BRAND_READ", "LOCATION_READ", "UOM_READ",
                "DISCOUNT_READ", "STOCK_CHECK_READ"
        ));

        Map<String, Permission> permissionMap = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        Map<String, Role> existingRoles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, r -> r));

        List<Role> rolesToSave = new ArrayList<>();
        for (Map.Entry<String, String> entry : roles.entrySet()) {
            String roleName = entry.getKey();
            Role role = existingRoles.get(roleName);

            Set<Permission> perms = rolePermissions.getOrDefault(roleName, Collections.emptyList()).stream()
                    .map(permissionMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (role == null) {
                role = new Role();
                role.setName(roleName);
                role.setDescription(entry.getValue());
                role.setPermissions(perms);
                rolesToSave.add(role);
                logger.debug("Creating role: {} with {} permissions", roleName, perms.size());
            } else {
                // Update permissions if needed
                Set<String> currentPerms = role.getPermissions().stream()
                        .map(Permission::getName).collect(Collectors.toSet());
                Set<String> expectedPerms = perms.stream()
                        .map(Permission::getName).collect(Collectors.toSet());
                if (!currentPerms.equals(expectedPerms)) {
                    role.setPermissions(perms);
                    rolesToSave.add(role);
                    logger.debug("Updating role: {} with {} permissions", roleName, perms.size());
                }
            }
        }

        if (!rolesToSave.isEmpty()) {
            roleRepository.saveAll(rolesToSave);
            logger.info("DevDataSeeder: Seeded/updated {} roles.", rolesToSave.size());
        } else {
            logger.info("DevDataSeeder: All {} roles already exist with correct permissions.", roles.size());
        }
    }

    private void seedDemoUsers() {
        logger.info("DevDataSeeder: Seeding demo users...");

        String[][] demoUsers = {
                {"admin", "admin@store.com", "admin123", "SUPER_ADMIN"},
                {"manager", "manager@store.com", "manager123", "STORE_MANAGER"}
        };

        Set<String> existingEmails = userRepository.findAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());

        Map<String, Role> roleMap = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, r -> r));

        int usersCreated = 0;

        for (String[] userData : demoUsers) {
            String firstName = userData[0];
            String email = userData[1];
            String password = userData[2];
            String roleName = userData[3];

            if (existingEmails.contains(email)) {
                logger.debug("Demo user {} already exists, skipping.", email);
                continue;
            }

            Role role = roleMap.get(roleName);
            if (role == null) {
                logger.warn("Role {} not found for demo user {}, skipping.", roleName, email);
                continue;
            }

            // Create user
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(firstName);
            user.setEmail(email);
            user.setUsername(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setActive(true);
            user.setCreatedBy("dev-seeder");
            user.setCreatedAt(LocalDateTime.now());

            // Create organization
            String orgName = generateUniqueOrgName(firstName + "'s Organization");
            Organization organization = new Organization();
            organization.setName(orgName);
            organization.setCreatedAt(LocalDateTime.now());
            organization.setCreatedBy("dev-seeder");
            organization = organizationRepository.save(organization);

            // Create default store
            Store store = new Store();
            store.setOrganization(organization);
            store.setName("Main Store");
            store.setLocation("Default Location");
            store.setStatus(StoreStatus.ACTIVE);
            store.setCreatedAt(LocalDateTime.now());
            store.setCreatedBy("dev-seeder");
            storeRepository.save(store);

            // Save user
            user = userRepository.save(user);

            // Create user-organization-role assignment
            UserOrganizationRole userOrgRole = new UserOrganizationRole();
            userOrgRole.setUserId(user.getId());
            userOrgRole.setOrganization(organization);
            userOrgRole.setRoleId(role.getId());
            userOrgRole.setCreatedAt(LocalDateTime.now());
            userOrgRole.setCreatedBy("dev-seeder");
            userOrganizationRoleRepository.save(userOrgRole);

            logger.info("Created demo user: {} with role {} in organization {}", email, roleName, orgName);
            usersCreated++;
        }

        if (usersCreated > 0) {
            logger.info("DevDataSeeder: Created {} demo users.", usersCreated);
        } else {
            logger.info("DevDataSeeder: All demo users already exist.");
        }
    }

    private String generateUniqueOrgName(String baseOrgName) {
        String orgName = baseOrgName;
        int suffix = 1;
        while (organizationRepository.findByName(orgName).isPresent()) {
            orgName = baseOrgName + " " + suffix++;
        }
        return orgName;
    }
}
