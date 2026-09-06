--liquibase formatted sql

--changeset codex:7 dbms:postgresql
with api_data(privilege_name, method_, url_) as (
    values
        ('Get location users', 'GET', '/master/location/users'),
        ('Remove user location', 'DELETE', '/master/location/user')
)
insert into m_privilege_master (
    uuid, privilege_name, method_, url_, created_at, updated_at,
    created_by, updated_by, is_active, is_deleted
)
select
    gen_random_uuid()::text, api.privilege_name, api.method_, api.url_,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, true, false
from api_data api
where not exists (
    select 1
    from m_privilege_master existing
    where existing.method_ = api.method_
      and existing.url_ = api.url_
      and existing.is_deleted = false
);

with privilege_access(method_, url_, access_name) as (
    values
        ('GET', '/master/location/users', 'Read'),
        ('DELETE', '/master/location/user', 'Delete')
)
insert into m_module_access_privilege_link (
    uuid, module_access_id, privilege_id, created_at, updated_at,
    created_by, updated_by, is_active, is_deleted
)
select
    gen_random_uuid()::text, access.id, privilege.id,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, true, false
from privilege_access mapping
join m_privilege_master privilege
  on privilege.method_ = mapping.method_
 and privilege.url_ = mapping.url_
 and privilege.is_deleted = false
join m_module_master module
  on module.module_name = 'User Location'
 and module.is_deleted = false
join m_module_access access
  on access.module_id = module.id
 and access.name = mapping.access_name
 and access.is_deleted = false
where not exists (
    select 1
    from m_module_access_privilege_link existing
    where existing.module_access_id = access.id
      and existing.privilege_id = privilege.id
      and existing.is_deleted = false
);
