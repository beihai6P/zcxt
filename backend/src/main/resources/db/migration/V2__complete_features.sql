create table if not exists asset_assembly (
    id varchar(32) primary key,
    asset_id varchar(32) not null,
    host_model varchar(50) not null,
    host_spec varchar(100),
    monitor_model varchar(50) not null,
    monitor_spec varchar(100),
    create_time datetime not null,
    update_time datetime not null
);

create unique index if not exists idx_asset_assembly_asset_id on asset_assembly (asset_id);

create table if not exists asset_consumable_stock (
    consumable_id varchar(32) primary key,
    consumable_type varchar(50) not null,
    consumable_name varchar(50) not null,
    stock_quantity int not null default 0,
    warning_threshold int not null default 10,
    create_time datetime not null,
    update_time datetime not null
);

create table if not exists asset_inventory (
    inventory_id varchar(32) primary key,
    inventory_name varchar(100) not null,
    scope_type varchar(20) not null,
    dept_id varchar(32),
    asset_type varchar(20),
    creator_id varchar(32) not null,
    status varchar(20) not null,
    start_time datetime not null,
    end_time datetime not null,
    create_time datetime not null,
    update_time datetime not null
);

create table if not exists asset_inventory_detail (
    detail_id varchar(32) primary key,
    inventory_id varchar(32) not null,
    asset_id varchar(32) not null,
    status varchar(20) not null,
    abnormal_type varchar(20),
    abnormal_reason varchar(255),
    check_time datetime,
    checker_id varchar(32)
);

create index if not exists idx_inv_detail_inv_id on asset_inventory_detail (inventory_id);

create table if not exists sys_permission (
    permission_id varchar(32) primary key,
    permission_name varchar(50) not null,
    permission_key varchar(100) not null
);

create table if not exists sys_role_permission (
    id varchar(32) primary key,
    role_id varchar(32) not null,
    permission_id varchar(32) not null
);

create index if not exists idx_sys_role_perm_role on sys_role_permission (role_id);

create table if not exists sys_log (
    log_id varchar(32) primary key,
    operator_id varchar(32),
    operator_name varchar(50),
    operate_time datetime not null,
    operate_module varchar(50) not null,
    operate_type varchar(20) not null,
    operate_content varchar(255),
    operate_ip varchar(50),
    operate_result varchar(10) not null,
    error_msg text
);

create index if not exists idx_sys_log_time on sys_log (operate_time);

create table if not exists asset_approval (
    approval_id varchar(32) primary key,
    asset_id varchar(32) not null,
    apply_type varchar(20) not null,
    apply_reason varchar(255),
    applicant_id varchar(32) not null,
    apply_time datetime not null,
    target_dept_id varchar(32),
    target_user_id varchar(32),
    status varchar(20) not null,
    approver_id varchar(32),
    approve_time datetime,
    approve_remark varchar(255)
);

create index if not exists idx_asset_approval_asset on asset_approval (asset_id);
create index if not exists idx_asset_approval_status on asset_approval (status);
