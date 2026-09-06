--liquibase formatted sql

--changeset pratik:7 dbms:postgresql
-- Add Location modules (User Location and Organization Location)
insert into m_module_master (
    uuid, module_name, description, parent_id, org_id, module_ui_name, url,
    created_at, updated_at, created_by, updated_by, is_active, is_deleted, is_master_entry
)
select
    gen_random_uuid()::text,
    md.module_name,
    md.description,
    parent.id,
    uol.org_id,
    md.module_ui_name,
    md.url_,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false,true
from user_organization_link uol
    join m_module_master parent
      on parent.org_id = uol.org_id
     and parent.module_name = 'Module Access'
     and parent.is_deleted = false
    cross join (
        values 
            ('User Location', 'Manage user locations', 'User Location', '/user-location-list'),
            ('Organization Location', 'Manage organization locations', 'Organization Location', '/org-location-list')
    ) as md(module_name, description, module_ui_name, url_)
where uol.user_id = 1
  and not exists (
      select 1
      from m_module_master existing
      where existing.module_name = md.module_name
        and existing.org_id = uol.org_id
        and existing.is_deleted = false
  );

-- Add module accesses (Read, Write, Delete) for Location modules
insert into m_module_access (
    uuid, name, module_id, created_at, updated_at, created_by, updated_by,
    is_active, is_deleted
)
select
    gen_random_uuid()::text,
    access_type.name,
    mm.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from m_module_master mm
    cross join (
        values ('Read'), ('Write'), ('Delete'),('Update'),('Download')
    ) as access_type(name)
where mm.module_name in ('User Location', 'Organization Location')
  and mm.org_id = (select org_id from user_organization_link where user_id = 1)
  and mm.is_deleted = false
  and not exists (
      select 1
      from m_module_access ma
      where ma.module_id = mm.id
        and ma.name = access_type.name
        and ma.is_deleted = false
  );

-- Grant Admin role access to Location modules
insert into m_role_module_access_link (
    uuid, role_id, module_access_id, created_at, updated_at, created_by,
    updated_by, is_active, is_deleted
)
select
    gen_random_uuid()::text,
    rm.id,
    ma.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from user_organization_link uol
    join m_role_master rm
      on rm.org_id = uol.org_id
     and rm.name = 'Admin'
     and rm.is_deleted = false
    join m_module_master mm
      on mm.org_id = uol.org_id
     and mm.module_name in ('User Location', 'Organization Location')
     and mm.is_deleted = false
    join m_module_access ma
      on ma.module_id = mm.id
     and ma.is_deleted = false
where uol.user_id = 1
  and not exists (
      select 1
      from m_role_module_access_link existing
      where existing.role_id = rm.id
        and existing.module_access_id = ma.id
        and existing.is_deleted = false
  );
