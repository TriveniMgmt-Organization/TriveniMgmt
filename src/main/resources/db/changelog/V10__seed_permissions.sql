--liquibase formatted sql

--changeset system:V10-seed-permissions
--comment: Seed industry-standard permissions for retail/inventory management

-- Organization permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000001', 'ORG_READ', 'Permission to read organization details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000002', 'ORG_WRITE', 'Permission to manage organization settings', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Store permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000003', 'STORE_READ', 'Permission to read store details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000004', 'STORE_WRITE', 'Permission to modify store settings', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Product permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000005', 'PRODUCT_READ', 'Permission to read product details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000006', 'PRODUCT_WRITE', 'Permission to modify product details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000007', 'COST_READ', 'Permission to view product cost information', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000008', 'MARGIN_READ', 'Permission to view profit margin information', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- User management permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000009', 'USER_READ', 'Permission to read user details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000010', 'USER_WRITE', 'Permission to modify user details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000011', 'USER_INVITE', 'Permission to invite new users to organization', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000012', 'ROLE_READ', 'Permission to read role details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000013', 'ROLE_WRITE', 'Permission to modify role assignments', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Inventory permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000014', 'INVENTORY_ITEM_READ', 'Permission to read inventory items', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000015', 'INVENTORY_ITEM_WRITE', 'Permission to modify inventory items', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000016', 'STOCK_ADJUST', 'Permission to adjust stock quantities', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000017', 'CYCLE_COUNT', 'Permission to perform inventory cycle counts', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000018', 'TRANSFER_CREATE', 'Permission to create stock transfers', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000019', 'TRANSFER_APPROVE', 'Permission to approve stock transfers', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Purchase Order permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000020', 'PO_READ', 'Permission to read purchase orders', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000021', 'PO_WRITE', 'Permission to create/modify purchase orders', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000022', 'PO_APPROVE', 'Permission to approve purchase orders', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000023', 'RECEIVING_CREATE', 'Permission to receive goods against POs', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Sales permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000024', 'SALE_READ', 'Permission to read sales data', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000025', 'SALE_WRITE', 'Permission to create/modify sales', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000026', 'VOID_SALE', 'Permission to void sales transactions', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000027', 'REFUND_APPROVE', 'Permission to approve refunds', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000028', 'PRICE_OVERRIDE', 'Permission to override item prices at POS', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000029', 'DISCOUNT_APPLY', 'Permission to apply discounts at POS', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Cash management permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000030', 'CASH_DRAWER_READ', 'Permission to view cash drawer status', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000031', 'CASH_DRAWER_WRITE', 'Permission to manage cash drawer operations', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000032', 'END_OF_DAY', 'Permission to perform end-of-day reconciliation', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Report permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000033', 'REPORT_READ', 'Permission to view basic reports', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000034', 'REPORT_FINANCIAL', 'Permission to view financial reports', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000035', 'REPORT_INVENTORY', 'Permission to view inventory reports', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Reference data permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000036', 'CATEGORY_READ', 'Permission to read category details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000037', 'CATEGORY_WRITE', 'Permission to modify category details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000038', 'BRAND_READ', 'Permission to read brand details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000039', 'BRAND_WRITE', 'Permission to modify brand details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000040', 'SUPPLIER_READ', 'Permission to read supplier details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000041', 'SUPPLIER_WRITE', 'Permission to modify supplier details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000042', 'LOCATION_READ', 'Permission to read location details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000043', 'LOCATION_WRITE', 'Permission to modify location details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000044', 'UOM_READ', 'Permission to read unit of measurement details', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000045', 'UOM_WRITE', 'Permission to modify unit of measurement details', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Other permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000046', 'DISCOUNT_READ', 'Permission to read discount configurations', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000047', 'DISCOUNT_WRITE', 'Permission to modify discount configurations', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000048', 'DAMAGE_LOSS_READ', 'Permission to read damage and loss records', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000049', 'DAMAGE_LOSS_WRITE', 'Permission to record damage and loss', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000050', 'STOCK_CHECK_READ', 'Permission to read stock check details', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

--rollback DELETE FROM permissions WHERE created_by = 'liquibase';
