package com.store.mgmt.seeder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.UUID;

public class Seeder {
    public static void main(String[] args) {
        // Configure data source
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/store_db");
        dataSource.setUsername(System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "postgres");
        dataSource.setPassword(System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "your_password");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Check if seeding is needed
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'admin'", Integer.class);
        if (userCount != null && userCount > 0) {
            System.out.println("Database already seeded, skipping...");
            return;
        }

        // Seed permissions
        jdbcTemplate.update("INSERT INTO permissions (id, name, description) VALUES (?, ?, ?)",
                UUID.randomUUID(), "CREATE_TRANSACTION", "Create new transactions in POS");
        jdbcTemplate.update("INSERT INTO permissions (id, name, description) VALUES (?, ?, ?)",
                UUID.randomUUID(), "VIEW_TRANSACTION", "View specific transaction details");
        // Add other permissions...

        // Seed roles
        String adminRoleId = UUID.randomUUID().toString();
        jdbcTemplate.update("INSERT INTO roles (id, name, description) VALUES (?, ?, ?)",
                adminRoleId, "ADMIN", "Full system access");
        String managerRoleId = UUID.randomUUID().toString();
        jdbcTemplate.update("INSERT INTO roles (id, name, description) VALUES (?, ?, ?)",
                managerRoleId, "MANAGER", "Store operations and limited user management");
        // Add other roles...

        // Seed role_permissions
        jdbcTemplate.update("INSERT INTO role_permissions (role_id, permission_id) " +
                "SELECT ?, id FROM permissions", adminRoleId);
        // Add other role_permissions...

        // Seed users
        String adminPasswordHash = "$2a$10$XURPShlN/0fs3Y/3r1r2xOa1T.JeZ4k5iL2Xq7O7D2f3b5k8j9m0q"; // admin123
        jdbcTemplate.update("INSERT INTO users (id, username, password_hash, email, created_at, is_active) " +
                        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, TRUE)",
                UUID.randomUUID(), "admin", adminPasswordHash, "admin@store.com");
        String managerPasswordHash = "$2a$10$z9k2h3j4k5l6m7n8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e4f5g6h"; // manager123
        jdbcTemplate.update("INSERT INTO users (id, username, password_hash, email, created_at, is_active) " +
                        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, TRUE)",
                UUID.randomUUID(), "manager", managerPasswordHash, "manager@store.com");

        // Seed user_roles
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) " +
                "SELECT id, ? FROM users WHERE username = 'admin'", adminRoleId);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) " +
                "SELECT id, ? FROM users WHERE username = 'manager'", managerRoleId);

        System.out.println("Database seeded successfully");
    }
}