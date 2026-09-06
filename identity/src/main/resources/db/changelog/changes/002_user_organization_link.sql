--liquibase formatted sql

--changeset pratik:2 dbms:postgresql

ALTER TABLE m_user
    DROP COLUMN IF EXISTS org_id;

ALTER TABLE user_organization_link
    ALTER COLUMN user_id SET NOT NULL,
    ALTER COLUMN org_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_organization_link_user_org
    ON user_organization_link (user_id, org_id);

INSERT INTO user_organization_link (user_id, org_id, created_at, created_by, updated_at, updated_by, is_active, is_deleted)
SELECT 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, true, false
WHERE NOT EXISTS (
    SELECT 1 FROM user_organization_link
    WHERE user_id = 1 AND org_id = 1
);
