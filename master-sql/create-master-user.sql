insert into spider.m_organisation(uuid,name,description,created_by,updated_by)
values (UUID(),'Loan Wired','Loan Wired Organisation','System','System');

-- Admin User
insert into spider.m_user(uuid,email,password,is_super_admin,created_by,updated_by,org_id)
values (UUID(),'admin@gmail.com',
'$2a$10$8m.6l8mCXcKqBga9jbgnluE8GhtPZqf7l8/aiqRdXOZwAVl6syQfu',1,'System','System',
(SELECT o.id from spider.m_organisation o where o.name='Loan Wired'));
-- Password is Admin@1234

-- Non admin user
insert into spider.m_user(uuid,email,password,is_super_admin,created_by,updated_by,org_id)
values (UUID(),'non-admin@gmail.com',
'$2a$10$8m.6l8mCXcKqBga9jbgnluE8GhtPZqf7l8/aiqRdXOZwAVl6syQfu',0,'System','System',
(SELECT o.id from spider.m_organisation o where o.name='Loan Wired'));
