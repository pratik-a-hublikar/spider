--liquibase formatted sql

--changeset pratik:1 dbms:postgresql

insert
into
    m_organisation
(
    created_at,
    created_by,
    is_active,
    is_deleted,
    updated_at,
    updated_by,
    "uuid",
    app_type,
    is_super_organization,
    parent_org)
values( CURRENT_TIMESTAMP, 1, true, false, CURRENT_TIMESTAMP, 1, 'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Super Organization', true, null);

with api_data(privilege_name, method_, url_) as (
    values
        ('User login',
         'POST',
         '/auth/login'),
        ('User login details', 'GET', '/auth/logins'),
        ('Get active sessions', 'GET', '/auth/identity/session'),
        ('Logout all sessions', 'PUT', '/api/auth/identity/session/logout/all'),
        ('Authorize session', 'GET', '/private/authorize'),
        ('Logout session', 'GET', '/api/identity/auth/logout'),
-- ModuleMasterRestController
        ('Create module',
         'POST',
         '/module'),
        ('Filter modules',
         'POST',
         '/module/filter'),
        ('Get module details',
         'GET',
         '/module/{moduleId}'),
        ('Get accessible modules',
         'GET',
         '/module/accessible'),
        ('Update module',
         'PUT',
         '/module/{moduleId}'),
        ('Delete module',
         'DELETE',
         '/module/{moduleId}'),
        ('Get parent modules', 'GET', '/module/parent'),
        ('Get child modules', 'GET', '/module/{moduleId}/children'),
        ('Get module access', 'GET', '/module/{moduleId}/access'),
        ('Get role modules', 'GET', '/module/role/{roleId}'),
        ('Get user modules', 'GET', '/module/app/{appId}/org/{orgId}/user/{userId}'),
-- PrivilegeRestController
        ('Filter privileges',
         'POST',
         '/privilege/filter'),
        ('Get privilege details',
         'GET',
         '/privilege/{privilegeId}'),
        ('Update privilege',
         'PUT',
         '/privilege'),
        ('Delete privilege',
         'DELETE',
         '/privilege/{privilegeId}'),
        ('Get all permissions',
         'GET',
         '/privilege/permission'),
        ('Create privilege',
         'POST',
         '/privilege'),
-- RolesRestController
        ('Filter roles', 'POST', '/master/role/filter'),
        ('Get role details', 'GET', '/master/role/{roleId}'),
        ('Create role', 'POST', '/master/role'),
        ('Update role', 'PUT', '/master/role'),
        ('Assign roles to user', 'POST', '/master/role/assign/{userId}'),
        ('Get roles by module access', 'GET', '/master/role/module-access/{moduleAccessId}'),
        ('Replace user role', 'PUT', '/master/role/{roleId}/org/{orgId}/user/{userId}'),
        ('Get all roles', 'GET', '/master/role'),
        ('Delete role', 'DELETE', '/master/role/{roleId}'),
        ('Remove user role', 'DELETE', '/master/role/org/{orgId}/user/{userId}'),
-- UserMasterRestController
        ('Filter users', 'POST', '/master/user/filter'),
        ('Get user details', 'GET', '/master/user/{userId}'),
        ('Create user', 'POST', '/master/user'),
        ('Update user', 'PUT', '/master/user/{userId}'),
        ('Delete user', 'DELETE', '/master/user/{userId}'),
        ('Get user by email', 'GET', '/master/user/email/{email}'),
        ('Get users by emails', 'POST', '/master/user/emails'),
        ('Update user email', 'PUT', '/master/user/update/email'),
        ('Reset user password', 'POST', '/master/user/{userId}/password'),
        ('Create users batch', 'POST', '/master/user/batch'),
        ('Delete users batch', 'DELETE', '/master/user/batch'),
        ('Filter organization users', 'POST', '/master/user/org/{orgId}/filter')
)
insert
into
	m_privilege_master (
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
    api.privilege_name,
    api.method_,
    api.url_,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    api_data api
where
    not exists (
        select
            1
        from
            m_privilege_master pm
        where
            pm.method_ = api.method_
          and pm.url_ = api.url_
          and pm.is_deleted = false
    );

insert
into
    m_user
(
    id,
    created_at,
    created_by,
    is_active,
    is_deleted,
    updated_at,
    updated_by,
    "uuid",
    email,
    password,
    fname,
    lname,
    user_name,
    is_mail_verified,
    is_otp_verified,
    status,
    is_super_admin)
select
    1,
    CURRENT_TIMESTAMP,
    1,
    true,
    false,
    CURRENT_TIMESTAMP,
    1,
    gen_random_uuid()::text,
    'admin@gmail.com',
    '$2a$10$OV51ftpF4TN93Hpw3xPGOOaIsXxAEwsOU22eeiU/UyyHFkLokdeFK',
    'Admin',
    'User',
    'admin',
    true,
    true,
    1,
    -1,
    true
from
    m_organisation o
where
    o.app_type = 'Super Organization';

insert into
    user_organization_link
(
    uuid,
    user_id,
    org_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
)
select
    gen_random_uuid()::text,
    1,
    o.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    m_organisation o
where
    o.app_type = 'Super Organization';

insert
into
    m_role_master (
    uuid,
    name,
    description,
    is_system_defined,
    org_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
)
select
    gen_random_uuid()::text,
    'Admin',
    'Administrator with access to all application modules',
    true,
    uol.org_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    user_organization_link uol
where
    uol.user_id = 1
  and not exists (
    select
        1
    from
        m_role_master rm
    where
        rm.name = 'Admin'
      and rm.org_id = uol.org_id
      and rm.is_deleted = false
);

insert
into
    m_user_role_link (
    uuid,
    user_id,
    role_id,
    org_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_active,
    is_deleted
)
select
    gen_random_uuid()::text,
    1,
    rm.id,
    rm.org_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    m_role_master rm
where
    rm.name = 'Admin'
  and rm.org_id = (
    select
        org_id
    from
        user_organization_link
    where
        user_id = 1)
  and rm.is_deleted = false
  and not exists (
    select
        1
    from
        m_user_role_link url
    where
        url.user_id = 1
      and url.role_id = rm.id
      and url.org_id = rm.org_id
      and url.is_deleted = false
);

insert
into
    m_module_master (
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
    'Manage Accesses',
    'Module for accessing application menus and APIs',
    null,
    uol.org_id,
    'Manage Accesses',
    '/manage_accesses',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false,true
from
    user_organization_link uol
where
    uol.user_id = 1
  and not exists (
    select
        1
    from
        m_module_master mm
    where
        mm.module_name = 'Manage Accesses'
      and mm.org_id = uol.org_id
      and mm.is_deleted = false
);

insert
into
    m_module_master (
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
    'Menus',
    'Module for accessing application menus and APIs',
    (
        select
            id
        from
            m_module_master
        where
            module_name = 'Manage Accesses'
          and is_deleted = false),
    uol.org_id,
    'Menu',
    '/menu-list',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false,true
from
    user_organization_link uol
where
    uol.user_id = 1
  and not exists (
    select
        1
    from
        m_module_master mm
    where
        mm.module_name = 'Menus'
      and mm.org_id = uol.org_id
      and mm.is_deleted = false
);

insert
into
    m_module_access (
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
    access_type.name,
    mm.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    m_module_master mm
        cross join (
        values
            ('Read'),
            ('Write'),
            ('Update'),
            ('Delete'),
            ('Download')
    ) as access_type(name)
where
    mm.module_name = 'Menus'
  and mm.org_id = (
    select
        org_id
    from
        user_organization_link
    where
        user_id = 1)
  and mm.is_deleted = false
  and not exists (
    select
        1
    from
        m_module_access ma
    where
        ma.module_id = mm.id
      and ma.name = access_type.name
      and ma.is_deleted = false
);

insert
into
    m_module_access (
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
    access_type.name,
    mm.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    m_module_master mm
        cross join (
        values
            ('Read')
    ) as access_type(name)
where
    mm.module_name = 'Manage Accesses'
  and mm.org_id = (
    select
        org_id
    from
        user_organization_link
    where
        user_id = 1)
  and mm.is_deleted = false
  and not exists (
    select
        1
    from
        m_module_access ma
    where
        ma.module_id = mm.id
      and ma.name = access_type.name
      and ma.is_deleted = false
);

insert
into
    m_role_module_access_link (
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
from
    m_role_master rm
        join m_module_master mm
             on
                 mm.org_id = rm.org_id
                     and mm.module_name = 'Menus'
                     and mm.is_deleted = false
        join m_module_access ma
             on
                 ma.module_id = mm.id
                     and ma.is_deleted = false
where
    rm.name = 'Admin'
  and rm.org_id = (
    select
        org_id
    from
        user_organization_link
    where
        user_id = 1)
  and rm.is_deleted = false
  and not exists (
    select
        1
    from
        m_role_module_access_link rmal
    where
        rmal.role_id = rm.id
      and rmal.module_access_id = ma.id
      and rmal.is_deleted = false
);

insert
into
    m_role_module_access_link (
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
from
    m_role_master rm
        join m_module_master mm
             on
                 mm.org_id = rm.org_id
                     and mm.module_name = 'Manage Accesses'
                     and mm.is_deleted = false
        join m_module_access ma
             on
                 ma.module_id = mm.id
                     and ma.is_deleted = false
where
    rm.name = 'Admin'
  and rm.org_id = (
    select
        org_id
    from
        user_organization_link
    where
        user_id = 1)
  and rm.is_deleted = false
  and not exists (
    select
        1
    from
        m_role_module_access_link rmal
    where
        rmal.role_id = rm.id
      and rmal.module_access_id = ma.id
      and rmal.is_deleted = false
);

with privilege_access(method_, url_, access_name) as (
    values
-- Authentication operations are treated as Read
('POST',
 '/auth/login',
 'Read'),
('GET', '/auth/logins', 'Read'),
('GET', '/auth/identity/session', 'Read'),
('PUT', '/api/auth/identity/session/logout/all', 'Read'),
('GET', '/private/authorize', 'Read'),
('GET', '/api/identity/auth/logout', 'Read'),
-- Module APIs
('POST',
 '/module',
 'Write'),
('POST',
 '/module/filter',
 'Read'),
('GET',
 '/module/{moduleId}',
 'Read'),
('GET',
 '/module/accessible',
 'Read'),
('PUT',
 '/module/{moduleId}',
 'Update'),
('DELETE',
 '/module/{moduleId}',
 'Delete'),
('GET', '/module/parent', 'Read'),
('GET', '/module/{moduleId}/children', 'Read'),
('GET', '/module/{moduleId}/access', 'Read'),
('GET', '/module/role/{roleId}', 'Read'),
('GET', '/module/app/{appId}/org/{orgId}/user/{userId}', 'Read'),
-- Privilege APIs
('POST',
 '/privilege/filter',
 'Read'),
('GET',
 '/privilege/{privilegeId}',
 'Read'),
('PUT',
 '/privilege',
 'Update'),
('DELETE',
 '/privilege/{privilegeId}',
 'Delete'),
('GET',
 '/privilege/permission',
 'Read'),
('POST',
 '/privilege',
 'Write'),
-- Role APIs
('POST', '/master/role/filter', 'Read'),
('GET', '/master/role/{roleId}', 'Read'),
('POST', '/master/role', 'Write'),
('PUT', '/master/role', 'Update'),
('POST', '/master/role/assign/{userId}', 'Update'),
('GET', '/master/role/module-access/{moduleAccessId}', 'Read'),
('PUT', '/master/role/{roleId}/org/{orgId}/user/{userId}', 'Update'),
('GET', '/master/role', 'Read'),
('DELETE', '/master/role/{roleId}', 'Delete'),
('DELETE', '/master/role/org/{orgId}/user/{userId}', 'Delete'),
-- User APIs
('POST', '/master/user/filter', 'Read'),
('GET', '/master/user/{userId}', 'Read'),
('POST', '/master/user', 'Write'),
('PUT', '/master/user/{userId}', 'Update'),
('DELETE', '/master/user/{userId}', 'Delete'),
('GET', '/master/user/email/{email}', 'Read'),
('POST', '/master/user/emails', 'Read'),
('PUT', '/master/user/update/email', 'Update'),
('POST', '/master/user/{userId}/password', 'Update'),
('POST', '/master/user/batch', 'Write'),
('DELETE', '/master/user/batch', 'Delete'),
('POST', '/master/user/org/{orgId}/filter', 'Read')
)
insert
into
	m_module_access_privilege_link (
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
from
    privilege_access pa
        join m_privilege_master pm
             on
                 pm.method_ = pa.method_
                     and pm.url_ = pa.url_
                     and pm.is_deleted = false
        join m_module_master mm
             on
                 mm.module_name = 'Roles'
                     and mm.org_id = (
                     select
                         org_id
                     from
                         user_organization_link
                     where
                         user_id = 1)
                     and mm.is_deleted = false
        join m_module_access ma
             on
                 ma.module_id = mm.id
                     and ma.name = pa.access_name
                     and ma.is_deleted = false
where
    not exists (
        select
            1
        from
            m_module_access_privilege_link mapl
        where
            mapl.module_access_id = ma.id
          and mapl.privilege_id = pm.id
          and mapl.is_deleted = false
    );


--changeset pratik:4 dbms:postgresql
-- Normalize the navigation hierarchy to:
-- Module Access
--   - Roles
--   - Modules
--   - User
update m_module_master
set module_name = 'Module Access',
    module_ui_name = 'Module Access',
    description = 'Manage roles, modules and users',
    url = null,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 1
where module_name = 'Manage Accesses'
  and org_id = (select org_id from user_organization_link where user_id = 1)
  and is_deleted = false;
--
-- update m_role_module_access_link
-- set is_active = false,
--     is_deleted = true,
--     updated_at = CURRENT_TIMESTAMP,
--     updated_by = 1
-- where module_access_id in (
--     select ma.id
--     from m_module_access ma
--         join m_module_master mm on mm.id = ma.module_id
--     where mm.module_name = 'Menus'
--       and mm.org_id = (select org_id from user_organization_link where user_id = 1)
--       and mm.is_deleted = false
-- );
--
-- update m_module_access
-- set is_active = false,
--     is_deleted = true,
--     updated_at = CURRENT_TIMESTAMP,
--     updated_by = 1
-- where module_id in (
--     select id
--     from m_module_master
--     where module_name = 'Menus'
--       and org_id = (select org_id from user_organization_link where user_id = 1)
--       and is_deleted = false
-- );
--
-- update m_module_master
-- set is_active = false,
--     is_deleted = true,
--     updated_at = CURRENT_TIMESTAMP,
--     updated_by = 1
-- where module_name = 'Menus'
--   and org_id = (select org_id from user_organization_link where user_id = 1)
--   and is_deleted = false;

with module_data(module_name, description, module_ui_name, url_) as (
    values
        ('Roles', 'Manage application roles and role access', 'Roles', '/role-list'),
        ('User', 'Manage application users', 'User', '/user-list')
)
insert into m_module_master (
    uuid, module_name, description, parent_id, org_id, module_ui_name, url,
    created_at, updated_at, created_by, updated_by, is_active, is_deleted,is_master_entry
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
    cross join module_data md
where uol.user_id = 1
  and not exists (
      select 1
      from m_module_master existing
      where existing.module_name = md.module_name
        and existing.org_id = uol.org_id
        and existing.is_deleted = false
  );

-- The parent needs Read access so it appears in navigation.
insert into m_module_access (
    uuid, name, module_id, created_at, updated_at, created_by, updated_by,
    is_active, is_deleted
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
where mm.module_name = 'Module Access'
  and mm.org_id = (select org_id from user_organization_link where user_id = 1)
  and mm.is_deleted = false
  and not exists (
      select 1
      from m_module_access ma
      where ma.module_id = mm.id
        and ma.name = 'Read'
        and ma.is_deleted = false
  );

-- Every child module receives the complete access set.
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
        values ('Read'), ('Write'), ('Update'), ('Delete'), ('Download')
    ) as access_type(name)
where mm.module_name in ('Roles', 'User')
  and mm.org_id = (select org_id from user_organization_link where user_id = 1)
  and mm.is_deleted = false
  and not exists (
      select 1
      from m_module_access ma
      where ma.module_id = mm.id
        and ma.name = access_type.name
        and ma.is_deleted = false
  );

-- User 1 owns the Admin role; grant that role every access on the parent and children.
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
     and mm.module_name in ('Module Access', 'Roles', 'User')
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
--
-- -- Rebuild API-to-access mappings module-wise.
-- update m_module_access_privilege_link
-- set is_active = true,
--     is_deleted = true,
--     updated_at = CURRENT_TIMESTAMP,
--     updated_by = 1
-- where module_access_id in (
--     select ma.id
--     from m_module_access ma
--         join m_module_master mm on mm.id = ma.module_id
--     where mm.module_name in ('Module Access', 'Roles', 'User')
--       and mm.org_id = (select org_id from user_organization_link where user_id = 1)
--       and mm.is_deleted = false
-- );

with privilege_access as (
    select
        pm.id as privilege_id,
        case
            when pm.url_ like '/master/role%' then 'Roles'
            when pm.url_ like '/master/user%' then 'User'
            when pm.url_ not like '/module%' or pm.url_ not like '/privilege%' then 'Module Access'
        end as module_name,
        case
            when pm.url_ not like '/master/role%'
              and pm.url_ not like '/master/user%'
              and pm.url_ not like '/module%'
              and pm.url_ not like '/privilege%' then 'Read'
            when pm.method_ = 'GET' then 'Read'
            when pm.method_ = 'DELETE' then 'Delete'
            when pm.method_ = 'PUT' then 'Update'
            when pm.url_ = '/auth/login'
              or pm.url_ like '%/filter'
              or pm.url_ = '/master/user/emails' then 'Read'
            when pm.url_ like '%/password'
              or pm.url_ like '%/assign/{userId}' then 'Update'
            else 'Write'
        end as access_name
    from m_privilege_master pm
    where pm.is_deleted = false
)
insert into m_module_access_privilege_link (
    uuid, module_access_id, privilege_id, created_at, updated_at, created_by,
    updated_by, is_active, is_deleted
)
select
    gen_random_uuid()::text,
    ma.id,
    pa.privilege_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from privilege_access pa
    join m_module_master mm
      on mm.module_name = pa.module_name
     and mm.org_id = (select org_id from user_organization_link where user_id = 1)
     and mm.is_deleted = false
    join m_module_access ma
      on ma.module_id = mm.id
     and ma.name = pa.access_name
     and ma.is_deleted = false
where not exists (
    select 1
    from m_module_access_privilege_link existing
    where existing.module_access_id = ma.id
      and existing.privilege_id = pa.privilege_id
      and existing.is_deleted = false
);


--changeset pratik:5 dbms:postgresql
insert
into
    m_module_master (
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
    is_deleted,is_master_entry
)
select
    gen_random_uuid()::text,
    'Roles',
    'Roles for accessing application menus and APIs',
    (
        select
            id
        from
            m_module_master
        where
            module_name = 'Manage Accesses'
          and is_deleted = false),
    uol.org_id,
    'Roles',
    '/role-list',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false,true
from
    user_organization_link uol
where
    uol.user_id = 1
  and not exists (
    select
        1
    from
        m_module_master mm
    where
        mm.module_name = 'Roles'
      and mm.org_id = uol.org_id
      and mm.is_deleted = false
);

insert
into
    m_module_access (
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
    access_type.name,
    mm.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1,
    true,
    false
from
    m_module_master mm
        cross join (
        values
            ('Read'),
            ('Write'),
            ('Update'),
            ('Delete'),
            ('Download')
    ) as access_type(name)
where
    mm.module_name = 'Roles'
  and mm.org_id = (
    select
        org_id
    from
        user_organization_link
    where
        user_id = 1)
  and mm.is_deleted = false
  and not exists (
    select
        1
    from
        m_module_access ma
    where
        ma.module_id = mm.id
      and ma.name = access_type.name
      and ma.is_deleted = false
);

--changeset pratik:6 dbms:postgresql

with privilege_access(method_, url_, module_name, access_name) as (
    select
        pm.method_,
        pm.url_,
        case
            when pm.url_ like '/master/role%' then 'Roles'
            when pm.url_ like '/master/user%' then 'User'
            when pm.url_ not like '/module%' or pm.url_ not like '/privilege%' then 'Module Access'
        end,
        case
            when pm.url_ not like '/master/role%'
              and pm.url_ not like '/master/user%'
              and pm.url_ not like '/module%'
              and pm.url_ not like '/privilege%' then 'Read'
            when pm.method_ = 'GET' then 'Read'
            when pm.method_ = 'DELETE' then 'Delete'
            when pm.method_ = 'PUT' then 'Update'
            when pm.url_ = '/auth/login'
                or pm.url_ like '%/filter'
                or pm.url_ = '/master/user/emails' then 'Read'
            when pm.url_ like '%/password'
                or pm.url_ like '%/assign/{userId}' then 'Update'
            else 'Write'
        end
    from
        m_privilege_master pm
    where
        pm.is_deleted = false
)
insert
into
    m_module_access_privilege_link (
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
from
    privilege_access pa
        join m_privilege_master pm
             on
                 pm.method_ = pa.method_
                     and pm.url_ = pa.url_
                     and pm.is_deleted = false
        join m_module_master mm
             on
                 mm.module_name = pa.module_name
                     and mm.org_id = (
                     select
                         org_id
                     from
                         user_organization_link
                     where
                         user_id = 1)
                     and mm.is_deleted = false
        join m_module_access ma
             on
                 ma.module_id = mm.id
                     and ma.name = pa.access_name
                     and ma.is_deleted = false
where
    not exists (
        select
            1
        from
            m_module_access_privilege_link mapl
        where
            mapl.module_access_id = ma.id
          and mapl.privilege_id = pm.id
          and mapl.is_deleted = false
    );
