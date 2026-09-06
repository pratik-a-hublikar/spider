--liquibase formatted sql

--changeset codex:11 dbms:postgresql
-- Restrict the Shipmnts fetch endpoint to the Sales Performance Read access.
insert into m_privilege_master (
    uuid,
    privilege_name,
    method_,
    url_,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
)
select
    gen_random_uuid()::text,
    'Fetch Shipmnts sales data',
    'GET',
    '/fetch',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
where not exists (
    select 1
    from m_privilege_master pm
    where pm.method_ = 'GET'
      and pm.url_ = '/fetch'
      and pm.is_deleted = false
);

insert into m_module_access_privilege_link (
    uuid,
    module_access_id,
    privilege_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
)
select
    gen_random_uuid()::text,
    ma.id,
    pm.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from m_module_master mm
join m_module_access ma
  on ma.module_id = mm.id
 and ma.name = 'Read'
 and ma.is_active = true
 and ma.is_deleted = false
join m_privilege_master pm
  on pm.method_ = 'GET'
 and pm.url_ = '/fetch'
 and pm.is_active = true
 and pm.is_deleted = false
where mm.module_name = 'Sales Performance'
  and mm.is_active = true
  and mm.is_deleted = false
  and not exists (
      select 1
      from m_module_access_privilege_link mapl
      where mapl.module_access_id = ma.id
        and mapl.privilege_id = pm.id
        and mapl.is_deleted = false
  );
