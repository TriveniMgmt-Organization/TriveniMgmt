package com.store.mgmt.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Production data seeder.
 *
 * Runs on non-dev profiles to seed platform-level reference data:
 * - Global templates (from JSON files in resources/seeds/globaltemplates)
 *
 * Permissions and roles are seeded by Liquibase migrations in production:
 * - V10__seed_permissions.sql
 * - V11__seed_roles.sql
 * - V12__seed_role_permissions.sql
 */
@Component
@Profile("!dev")
@Order(1)
public class ProductionDataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductionDataSeeder.class);

    private final GlobalTemplateSeeder globalTemplateSeeder;

    public ProductionDataSeeder(GlobalTemplateSeeder globalTemplateSeeder) {
        this.globalTemplateSeeder = globalTemplateSeeder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("ProductionDataSeeder: Starting production data seeding...");

        try {
            seedGlobalTemplates();
            logger.info("ProductionDataSeeder: Production data seeding completed successfully.");
        } catch (Exception e) {
            logger.error("ProductionDataSeeder: Failed to seed production data: {}", e.getMessage(), e);
            logger.warn("ProductionDataSeeder: Application will continue despite seeding failure.");
        }
    }

    private void seedGlobalTemplates() {
        logger.info("ProductionDataSeeder: Seeding global templates...");
        int count = globalTemplateSeeder.seedGlobalTemplates();
        if (count > 0) {
            logger.info("ProductionDataSeeder: Seeded {} global templates.", count);
        } else {
            logger.info("ProductionDataSeeder: All global templates already exist.");
        }
    }
}
