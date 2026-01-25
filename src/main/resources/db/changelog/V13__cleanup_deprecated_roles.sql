--liquibase formatted sql

--changeset system:V13-cleanup-deprecated-roles
--comment: Migrate deprecated roles to new industry-standard roles and soft-delete old ones

-- Step 1: Create mapping for user_organization_roles with deprecated roles
-- ADMIN users -> ORG_ADMIN (deprecated ADMIN role is merged into ORG_ADMIN)
UPDATE user_organization_roles
SET role_id = (SELECT id FROM roles WHERE name = 'ORG_ADMIN')
WHERE role_id = (SELECT id FROM roles WHERE name = 'ADMIN' AND deleted_at IS NULL);

-- MANAGER users -> STORE_MANAGER (deprecated MANAGER role is split/renamed to STORE_MANAGER)
UPDATE user_organization_roles
SET role_id = (SELECT id FROM roles WHERE name = 'STORE_MANAGER')
WHERE role_id = (SELECT id FROM roles WHERE name = 'MANAGER' AND deleted_at IS NULL);

-- SUPPORT users -> STAFF (deprecated SUPPORT role is renamed to STAFF)
UPDATE user_organization_roles
SET role_id = (SELECT id FROM roles WHERE name = 'STAFF')
WHERE role_id = (SELECT id FROM roles WHERE name = 'SUPPORT' AND deleted_at IS NULL);

-- Step 2: Also update user_roles table (legacy table, may still be in use)
UPDATE user_roles
SET role_id = (SELECT id FROM roles WHERE name = 'ORG_ADMIN')
WHERE role_id IN (SELECT id FROM roles WHERE name = 'ADMIN' AND deleted_at IS NULL);

UPDATE user_roles
SET role_id = (SELECT id FROM roles WHERE name = 'STORE_MANAGER')
WHERE role_id IN (SELECT id FROM roles WHERE name = 'MANAGER' AND deleted_at IS NULL);

UPDATE user_roles
SET role_id = (SELECT id FROM roles WHERE name = 'STAFF')
WHERE role_id IN (SELECT id FROM roles WHERE name = 'SUPPORT' AND deleted_at IS NULL);

-- Step 3: Soft-delete deprecated roles
UPDATE roles
SET deleted_at = CURRENT_TIMESTAMP,
    deleted_by = 'liquibase',
    description = CONCAT('[DEPRECATED - migrated to industry-standard roles] ', COALESCE(description, ''))
WHERE name IN ('ADMIN', 'MANAGER', 'SUPPORT', 'CUSTOMER')
  AND deleted_at IS NULL;

-- Step 4: Clean up role_permissions for deprecated roles
-- First, get the deprecated role IDs and delete their permission mappings
DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ADMIN', 'MANAGER', 'SUPPORT', 'CUSTOMER'));

--rollback UPDATE roles SET deleted_at = NULL, deleted_by = NULL WHERE name IN ('ADMIN', 'MANAGER', 'SUPPORT', 'CUSTOMER');
