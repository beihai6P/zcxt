-- 添加用户管理权限
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-user-manage', '用户管理', 'user:manage'
where not exists (select 1 from sys_permission where permission_id = 'perm-user-manage');

-- 为超级管理员添加用户管理权限
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-user-manage', 'role-super', 'perm-user-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-super-user-manage');

-- 为资产管理员添加用户管理权限
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-user-manage', 'role-asset-admin', 'perm-user-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-user-manage');
