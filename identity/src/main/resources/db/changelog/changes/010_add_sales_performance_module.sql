--liquibase formatted sql

--changeset codex:10 dbms:postgresql
-- Add the Sales Performance page as a top-level menu for Admin roles.
insert into m_module_master (
    uuid,
    module_name,
    description,
    parent_id,
    org_id,
    module_ui_name,
    url,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted,
    is_master_entry
)
select
    gen_random_uuid()::text,
    'Sales Performance',
    'View the sales performance report',
    null,
    admin_org.org_id,
    'Sales Performance',
    '/sales-performance',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false,
    true
from (
    select distinct org_id
    from m_role_master
    where name = 'Admin'
      and is_active = true
      and is_deleted = false
) admin_org
where
  not exists (
      select 1
      from m_module_master mm
      where mm.module_name = 'Sales Performance'
        and mm.org_id = admin_org.org_id
        and mm.is_deleted = false
  );

insert into m_module_access (
    uuid,
    name,
    module_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
)
select
    gen_random_uuid()::text,
    'Read',
    mm.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from m_module_master mm
where mm.module_name = 'Sales Performance'
  and mm.is_active = true
  and mm.is_deleted = false
  and not exists (
      select 1
      from m_module_access ma
      where ma.module_id = mm.id
        and ma.name = 'Read'
        and ma.is_deleted = false
  );

insert into m_role_module_access_link (
    uuid,
    role_id,
    module_access_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
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
from m_role_master rm
join m_module_master mm
  on mm.org_id = rm.org_id
 and mm.module_name = 'Sales Performance'
 and mm.is_active = true
 and mm.is_deleted = false
join m_module_access ma
  on ma.module_id = mm.id
 and ma.name = 'Read'
 and ma.is_active = true
 and ma.is_deleted = false
where rm.name = 'Admin'
  and rm.is_active = true
  and rm.is_deleted = false
  and not exists (
      select 1
      from m_role_module_access_link rmal
      where rmal.role_id = rm.id
        and rmal.module_access_id = ma.id
        and rmal.is_deleted = false
  );
