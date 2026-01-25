--liquibase formatted sql

--changeset system:V11-seed-roles
--comment: Seed industry-standard roles for retail/inventory management

-- Platform-level role
INSERT INTO roles (id, name, description, created_at, created_by) VALUES
    ('22222222-2222-2222-2222-000000000001', 'SUPER_ADMIN', 'Platform administrator with full system access', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Organization-level roles
INSERT INTO roles (id, name, description, created_at, created_by) VALUES
    ('22222222-2222-2222-2222-000000000002', 'ORG_ADMIN', 'Organization owner with full access to organization settings and users', CURRENT_TIMESTAMP, 'liquibase'),
    ('22222222-2222-2222-2222-000000000008', 'ACCOUNTANT', 'Financial reporting and accounting access at organization level', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Store-level roles
INSERT INTO roles (id, name, description, created_at, created_by) VALUES
    ('22222222-2222-2222-2222-000000000003', 'STORE_MANAGER', 'Full store management access including staff, inventory, and operations', CURRENT_TIMESTAMP, 'liquibase'),
    ('22222222-2222-2222-2222-000000000004', 'SHIFT_LEAD', 'Shift supervisor with limited management capabilities', CURRENT_TIMESTAMP, 'liquibase'),
    ('22222222-2222-2222-2222-000000000005', 'CASHIER', 'Point-of-sale operations and basic inventory viewing', CURRENT_TIMESTAMP, 'liquibase'),
    ('22222222-2222-2222-2222-000000000006', 'INVENTORY_SPECIALIST', 'Stock management, receiving, and inventory operations', CURRENT_TIMESTAMP, 'liquibase'),
    ('22222222-2222-2222-2222-000000000007', 'PURCHASING_AGENT', 'Vendor management and purchase order operations', CURRENT_TIMESTAMP, 'liquibase'),
    ('22222222-2222-2222-2222-000000000009', 'STAFF', 'General read access for basic store operations', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

--rollback DELETE FROM roles WHERE created_by = 'liquibase';
