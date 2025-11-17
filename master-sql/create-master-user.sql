insert into spider.m_user(uuid,email,password,is_super_admin,created_by,updated_by)
values (UUID(),'admin@gmail.com','$2a$10$8m.6l8mCXcKqBga9jbgnluE8GhtPZqf7l8/aiqRdXOZwAVl6syQfu',1,'System','System');

-- Password is Admin@1234