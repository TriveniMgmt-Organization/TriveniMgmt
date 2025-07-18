package com.store.mgmt.config;

import com.store.mgmt.users.model.entity.Permission;
import com.store.mgmt.users.model.entity.Role;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.PermissionRepository;
import com.store.mgmt.users.repository.RoleRepository;
import com.store.mgmt.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository,
                      PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
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

        long permissionCount = permissionRepository.count();
        List<Permission> newPermissions = new ArrayList<>();

        if (permissionCount == 0) {
            // Table is empty, seed all permissions
            for (Map.Entry<String, String> entry : permissions.entrySet()) {
                Permission permission = new Permission();
                permission.setName(entry.getKey());
                permission.setDescription(entry.getValue());
                newPermissions.add(permission);
            }
        } else {
            // Table has data, check for missing permissions
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
        Map<String, String> roles = new HashMap<>();
        roles.put("SUPER_ADMIN", "Role with all permissions and administrative access");
        roles.put("ADMIN", "Role with administrative access to manage users and resources");
        roles.put("MANAGER", "Role with permissions to manage sales and inventory");
        roles.put("CASHIER", "Role with permissions to handle sales transactions");
        roles.put("SUPPORT", "Role with permissions to assist users");
        roles.put("CUSTOMER", "Role with permissions to view discounts and stock availability");

        Map<String, List<String>> rolePermissions = new HashMap<>();
        rolePermissions.put("SUPER_ADMIN", Arrays.asList(
                "PRODUCT_READ", "PRODUCT_WRITE", "USER_READ", "USER_WRITE", "ROLE_READ", "ROLE_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "CATEGORY_READ", "CATEGORY_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ", "UOM_WRITE",
                "PO_READ", "PO_WRITE", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "DISCOUNT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ", "REPORT_READ"
        ));
        rolePermissions.put("ADMIN", Arrays.asList(
                "PRODUCT_READ", "PRODUCT_WRITE", "USER_READ", "USER_WRITE", "ROLE_READ", "ROLE_WRITE",
                "INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE", "CATEGORY_READ", "CATEGORY_WRITE",
                "SUPPLIER_READ", "SUPPLIER_WRITE", "LOCATION_READ", "LOCATION_WRITE", "UOM_READ", "UOM_WRITE",
                "PO_READ", "PO_WRITE", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "DISCOUNT_WRITE",
                "DAMAGE_LOSS_READ", "DAMAGE_LOSS_WRITE", "STOCK_CHECK_READ", "REPORT_READ"
        ));
        rolePermissions.put("MANAGER", Arrays.asList(
                "PRODUCT_READ", "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "STOCK_CHECK_READ",
                "INVENTORY_ITEM_READ", "CATEGORY_READ", "SUPPLIER_READ", "LOCATION_READ",
                "UOM_READ", "PO_READ", "SALE_READ", "DISCOUNT_READ", "DAMAGE_LOSS_READ", "STOCK_CHECK_READ"
        ));
        rolePermissions.put("CASHIER", Arrays.asList(
                "SALE_READ", "SALE_WRITE", "DISCOUNT_READ", "STOCK_CHECK_READ"
        ));
        rolePermissions.put("SUPPORT", Arrays.asList(
                "USER_READ", "USER_WRITE"
        ));
        rolePermissions.put("CUSTOMER", Arrays.asList(
                "DISCOUNT_READ", "STOCK_CHECK_READ"
        ));

        long roleCount = roleRepository.count();
        List<Role> rolesToSave = new ArrayList<>();

        // Fetch all permissions in one query if needed
        Map<String, Permission> permissionMap = permissionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Permission::getName, p -> p));

        if (roleCount == 0) {
            // Table is empty, seed all roles
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

//                if (permissionSet.isEmpty()) {
//                    continue;
//                }

                role.setPermissions(permissionSet);
                rolesToSave.add(role);
            }
        } else {
            // Table has data, check for missing roles
            Map<String, Role> existingRoles = roleRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(Role::getName, r -> r));

            for (Map.Entry<String, String> entry : roles.entrySet()) {
                String roleName = entry.getKey();
                Role role = existingRoles.getOrDefault(roleName, new Role());
                role.setName(roleName);
                role.setDescription(entry.getValue());

                List<String> permissionNames = rolePermissions.getOrDefault(roleName, Collections.emptyList());
                Set<Permission> permissionSet = permissionNames.stream()
                        .map(name -> permissionMap.get(name))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

//                if (permissionSet.isEmpty()) {
//                    continue;
//                }

                role.setPermissions(permissionSet);
                rolesToSave.add(role);
            }
        }

        if (!rolesToSave.isEmpty()) {
            roleRepository.saveAll(rolesToSave);
            logger.info("Seeded/Updated {} roles", rolesToSave.size());
        } else {
            logger.debug("No new roles to seed");
        }
    }

    private void seedUsers() {
        logger.debug("Seeding users...");
        String[][] users = {
                {"admin", "admin@store.com", "admin123", "SUPER_ADMIN"},
                {"manager", "manager@store.com", "manager123", "MANAGER"}
        };

        long userCount = userRepository.count();
        List<User> newUsers = new ArrayList<>();

        // Fetch all roles in one query if needed
        Map<String, Role> roleMap = roleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Role::getName, r -> r));

        if (userCount == 0) {
            // Table is empty, seed all users
            for (String[] userData : users) {
                User user = new User();
                user.setFirstName(userData[0]);
                user.setUsername(userData[1]);
                user.setEmail(userData[1]);
                user.setPasswordHash(passwordEncoder.encode(userData[2]));
                user.setActive(true);
                user.setCreatedBy("system");

                Role role = roleMap.get(userData[3]);
//                if (role == null) {
//                    continue;
//                }
                user.setRoles(new HashSet<>(Collections.singletonList(role)));
                newUsers.add(user);
            }
        } else {
            // Table has data, check for missing users
            Set<String> existingUserEmails = userRepository.findAll()
                    .stream()
                    .map(User::getEmail)
                    .collect(Collectors.toSet());

            for (String[] userData : users) {
                String email = userData[1];
                if (!existingUserEmails.contains(email)) {
                    User user = new User();
                    user.setFirstName(userData[0]);
                    user.setLastName(userData[0]);
                    user.setUsername(userData[1]);
                    user.setEmail(email);
                    user.setPasswordHash(passwordEncoder.encode(userData[2]));
                    user.setActive(true);
                    user.setCreatedBy("system");

                    Role role = roleMap.get(userData[3]);
//                    if (role == null) {
//                        continue;
//                    }
                    user.setRoles(new HashSet<>(Collections.singletonList(role)));
                    newUsers.add(user);
                }
            }
        }
        if (!newUsers.isEmpty()) {
            userRepository.saveAll(newUsers);
            logger.info("Seeded {} new users", newUsers.size());
        } else {
            logger.debug("No new users to seed");
        }
    }
}