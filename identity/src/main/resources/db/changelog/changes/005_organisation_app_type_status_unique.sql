--liquibase formatted sql

--changeset pratik:5 dbms:postgresql

ALTER TABLE m_organisation
    DROP CONSTRAINT IF EXISTS m_organisation_app_type_key;
ALTER TABLE m_organisation
DROP CONSTRAINT IF EXISTS uq_organisation_app_type_status;

ALTER TABLE m_organisation
    ADD CONSTRAINT uq_organisation_app_type_status
        UNIQUE (app_type, is_active, is_deleted);
