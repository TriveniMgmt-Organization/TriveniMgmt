package com.store.mgmt.config;

import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaPermissionRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Data validation component.
 *
 * This component validates that reference data exists on startup.
 * Uses ApplicationRunner to ensure it runs AFTER database is ready.
 *
 * Data seeding strategy:
 * - DEV PROFILE: DevDataSeeder seeds permissions, roles, demo users, and global templates
 * - PROD PROFILE: Liquibase seeds permissions/roles; ProductionDataSeeder seeds global templates
 *
 * Reference data files:
 * - Liquibase: V10__seed_permissions.sql, V11__seed_roles.sql, V12__seed_role_permissions.sql
 * - Templates: resources/seeds/globaltemplates/*.json
 */
@Component
@Order(0) // Run before DevDataSeeder
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final JpaPermissionRepository permissionRepository;
    private final JpaRoleRepository roleRepository;

    public DataSeeder(
            JpaPermissionRepository permissionRepository,
            JpaRoleRepository roleRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("DataSeeder: Validating reference data...");

        long permissionCount = permissionRepository.count();
        long roleCount = roleRepository.count();

        if (permissionCount == 0) {
            logger.warn("No permissions found in database. Liquibase migrations may not have run.");
        } else {
            logger.info("DataSeeder: Found {} permissions.", permissionCount);
        }

        if (roleCount == 0) {
            logger.warn("No roles found in database. Liquibase migrations may not have run.");
        } else {
            logger.info("DataSeeder: Found {} roles.", roleCount);
        }

        if (permissionCount > 0 && roleCount > 0) {
            logger.info("DataSeeder: Reference data validation successful.");
        }

        logger.info("DataSeeder: Reference data managed by Liquibase. Dev data via DevDataSeeder (@Profile(\"dev\")).");
    }
}
