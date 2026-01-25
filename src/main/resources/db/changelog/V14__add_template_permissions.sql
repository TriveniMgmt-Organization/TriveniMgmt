--liquibase formatted sql

--changeset system:V14-add-template-permissions
--comment: Add template management permissions for global templates feature

-- Template permissions
INSERT INTO permissions (id, name, description, created_at, created_by) VALUES
    ('11111111-1111-1111-1111-000000000051', 'TEMPLATE_READ', 'Permission to view global templates', CURRENT_TIMESTAMP, 'liquibase'),
    ('11111111-1111-1111-1111-000000000052', 'TEMPLATE_WRITE', 'Permission to manage and apply templates', CURRENT_TIMESTAMP, 'liquibase')
ON CONFLICT (name) DO NOTHING;

-- Grant template permissions to SUPER_ADMIN (all templates access)
INSERT INTO role_permissions (role_id, permission_id)
SELECT '22222222-2222-2222-2222-000000000001', id FROM permissions
WHERE name IN ('TEMPLATE_READ', 'TEMPLATE_WRITE')
ON CONFLICT DO NOTHING;

-- Grant template permissions to ORG_ADMIN (can apply templates to their organization)
INSERT INTO role_permissions (role_id, permission_id)
SELECT '22222222-2222-2222-2222-000000000002', id FROM permissions
WHERE name IN ('TEMPLATE_READ', 'TEMPLATE_WRITE')
ON CONFLICT DO NOTHING;

--rollback DELETE FROM role_permissions WHERE permission_id IN (SELECT id FROM permissions WHERE name IN ('TEMPLATE_READ', 'TEMPLATE_WRITE'));
--rollback DELETE FROM permissions WHERE name IN ('TEMPLATE_READ', 'TEMPLATE_WRITE');
