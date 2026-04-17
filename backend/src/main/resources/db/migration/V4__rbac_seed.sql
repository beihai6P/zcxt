insert into sys_role (role_id, role_name, create_time, update_time)
select 'role-asset-admin', '资产管理员', current_timestamp, current_timestamp
where not exists (select 1 from sys_role where role_id = 'role-asset-admin');

insert into sys_role (role_id, role_name, create_time, update_time)
select 'role-user', '普通用户', current_timestamp, current_timestamp
where not exists (select 1 from sys_role where role_id = 'role-user');

insert into sys_role (role_id, role_name, create_time, update_time)
select 'role-manager', '管理层', current_timestamp, current_timestamp
where not exists (select 1 from sys_role where role_id = 'role-manager');

insert into sys_user (user_id, username, password_hash, display_name, dept_id, role_id, email, enabled, create_time, update_time)
select 'user-asset-admin', 'assetadmin', '$2a$10$jiWMXRhBGb4yVf0OWG5xS.3dFAgGB7lsgwL4PdSqF6QTR0S1oT.7a', '资产管理员', 'dept-it', 'role-asset-admin', null, true, current_timestamp, current_timestamp
where not exists (select 1 from sys_user where user_id = 'user-asset-admin');

insert into sys_user (user_id, username, password_hash, display_name, dept_id, role_id, email, enabled, create_time, update_time)
select 'user-normal', 'user', '$2a$10$jiWMXRhBGb4yVf0OWG5xS.3dFAgGB7lsgwL4PdSqF6QTR0S1oT.7a', '普通用户', 'dept-it', 'role-user', null, true, current_timestamp, current_timestamp
where not exists (select 1 from sys_user where user_id = 'user-normal');

insert into sys_user (user_id, username, password_hash, display_name, dept_id, role_id, email, enabled, create_time, update_time)
select 'user-manager', 'manager', '$2a$10$jiWMXRhBGb4yVf0OWG5xS.3dFAgGB7lsgwL4PdSqF6QTR0S1oT.7a', '管理层', 'dept-it', 'role-manager', null, true, current_timestamp, current_timestamp
where not exists (select 1 from sys_user where user_id = 'user-manager');

insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-asset-read', '资产查询', 'asset:read'
where not exists (select 1 from sys_permission where permission_id = 'perm-asset-read');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-asset-write', '资产维护', 'asset:write'
where not exists (select 1 from sys_permission where permission_id = 'perm-asset-write');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-approval-read', '审批查看', 'approval:read'
where not exists (select 1 from sys_permission where permission_id = 'perm-approval-read');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-approval-approve', '审批处理', 'approval:approve'
where not exists (select 1 from sys_permission where permission_id = 'perm-approval-approve');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-inventory-manage', '盘点管理', 'inventory:manage'
where not exists (select 1 from sys_permission where permission_id = 'perm-inventory-manage');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-consumable-manage', '耗材管理', 'consumable:manage'
where not exists (select 1 from sys_permission where permission_id = 'perm-consumable-manage');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-stats-view', '大屏查看', 'stats:view'
where not exists (select 1 from sys_permission where permission_id = 'perm-stats-view');
insert into sys_permission (permission_id, permission_name, permission_key)
select 'perm-sys-manage', '系统管理', 'sys:manage'
where not exists (select 1 from sys_permission where permission_id = 'perm-sys-manage');

insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-asset-read', 'role-super', 'perm-asset-read'
where not exists (select 1 from sys_role_permission where id = 'rp-super-asset-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-asset-write', 'role-super', 'perm-asset-write'
where not exists (select 1 from sys_role_permission where id = 'rp-super-asset-write');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-approval-read', 'role-super', 'perm-approval-read'
where not exists (select 1 from sys_role_permission where id = 'rp-super-approval-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-approval-approve', 'role-super', 'perm-approval-approve'
where not exists (select 1 from sys_role_permission where id = 'rp-super-approval-approve');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-inventory-manage', 'role-super', 'perm-inventory-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-super-inventory-manage');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-consumable-manage', 'role-super', 'perm-consumable-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-super-consumable-manage');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-stats-view', 'role-super', 'perm-stats-view'
where not exists (select 1 from sys_role_permission where id = 'rp-super-stats-view');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-super-sys-manage', 'role-super', 'perm-sys-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-super-sys-manage');

insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-asset-read', 'role-asset-admin', 'perm-asset-read'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-asset-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-asset-write', 'role-asset-admin', 'perm-asset-write'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-asset-write');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-approval-read', 'role-asset-admin', 'perm-approval-read'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-approval-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-approval-approve', 'role-asset-admin', 'perm-approval-approve'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-approval-approve');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-inventory-manage', 'role-asset-admin', 'perm-inventory-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-inventory-manage');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-consumable-manage', 'role-asset-admin', 'perm-consumable-manage'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-consumable-manage');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-asset-admin-stats-view', 'role-asset-admin', 'perm-stats-view'
where not exists (select 1 from sys_role_permission where id = 'rp-asset-admin-stats-view');

insert into sys_role_permission (id, role_id, permission_id)
select 'rp-user-asset-read', 'role-user', 'perm-asset-read'
where not exists (select 1 from sys_role_permission where id = 'rp-user-asset-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-user-approval-read', 'role-user', 'perm-approval-read'
where not exists (select 1 from sys_role_permission where id = 'rp-user-approval-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-user-stats-view', 'role-user', 'perm-stats-view'
where not exists (select 1 from sys_role_permission where id = 'rp-user-stats-view');

insert into sys_role_permission (id, role_id, permission_id)
select 'rp-manager-asset-read', 'role-manager', 'perm-asset-read'
where not exists (select 1 from sys_role_permission where id = 'rp-manager-asset-read');
insert into sys_role_permission (id, role_id, permission_id)
select 'rp-manager-stats-view', 'role-manager', 'perm-stats-view'
where not exists (select 1 from sys_role_permission where id = 'rp-manager-stats-view');

