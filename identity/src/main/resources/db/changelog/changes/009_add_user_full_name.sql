--liquibase formatted sql

--changeset pratik:9 dbms:postgresql

ALTER TABLE m_user
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(511);

UPDATE m_user
SET full_name = CONCAT_WS(' ', NULLIF(BTRIM(fname), ''), NULLIF(BTRIM(lname), ''))
WHERE full_name IS NULL
   OR BTRIM(full_name) = '';

ALTER TABLE m_user
    ALTER COLUMN full_name SET NOT NULL;
