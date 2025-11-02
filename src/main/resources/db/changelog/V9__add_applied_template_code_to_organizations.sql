-- V9: Add applied_template_code column to organizations table
-- This tracks which global template was applied (one-time operation)

ALTER TABLE organizations
ADD COLUMN applied_template_code VARCHAR(50) NULL;

COMMENT ON COLUMN organizations.applied_template_code IS 'Code of the global template applied to this organization. Template application is a one-time operation.';

