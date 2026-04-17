create table if not exists sys_dept (
    dept_id varchar(32) primary key,
    dept_name varchar(100) not null,
    parent_dept_id varchar(32),
    create_time datetime not null,
    update_time datetime not null
);

create table if not exists sys_role (
    role_id varchar(32) primary key,
    role_name varchar(50) not null,
    create_time datetime not null,
    update_time datetime not null
);

create table if not exists sys_user (
    user_id varchar(32) primary key,
    username varchar(50) not null,
    password_hash varchar(100) not null,
    display_name varchar(50) not null,
    dept_id varchar(32),
    role_id varchar(32) not null,
    email varchar(100),
    enabled boolean not null,
    create_time datetime not null,
    update_time datetime not null
);

create unique index if not exists idx_sys_user_username on sys_user (username);

create table if not exists asset_base (
    asset_id varchar(32) primary key,
    asset_type varchar(20) not null,
    asset_name varchar(50) not null,
    model varchar(50) not null,
    specification varchar(100),
    purchase_date date not null,
    purchase_price decimal(10,2),
    supplier varchar(50),
    dept_id varchar(32) not null,
    user_id varchar(32),
    status varchar(20) not null,
    qrcode_url varchar(255) not null,
    qrcode_content text not null,
    image_url varchar(255),
    remark text,
    create_time datetime not null,
    update_time datetime not null
);

create index if not exists idx_asset_base_type on asset_base (asset_type);
create index if not exists idx_asset_base_status on asset_base (status);
create index if not exists idx_asset_base_dept on asset_base (dept_id);
create index if not exists idx_asset_base_user on asset_base (user_id);

create table if not exists asset_change_history (
    history_id varchar(32) primary key,
    asset_id varchar(32) not null,
    change_type varchar(20) not null,
    old_info text not null,
    new_info text not null,
    operator_id varchar(32) not null,
    approver_id varchar(32),
    change_time datetime not null,
    reason varchar(255)
);

create index if not exists idx_ach_asset_id on asset_change_history (asset_id);
create index if not exists idx_ach_change_time on asset_change_history (change_time);

create table if not exists asset_stats_daily (
    id varchar(32) primary key,
    stat_date date not null,
    total int not null,
    in_use int not null,
    idle int not null,
    repairing int not null,
    scrapped int not null,
    create_time datetime not null
);

create unique index if not exists idx_asset_stats_daily_date on asset_stats_daily (stat_date);

create table if not exists ai_warning (
    warning_id varchar(32) primary key,
    warning_type varchar(50) not null,
    content varchar(500) not null,
    warning_time datetime not null,
    status varchar(20) not null,
    handler_id varchar(32),
    handle_time datetime
);

insert into sys_role (role_id, role_name, create_time, update_time)
select 'role-super', '超级管理员', current_timestamp, current_timestamp
where not exists (select 1 from sys_role where role_id = 'role-super');

insert into sys_dept (dept_id, dept_name, parent_dept_id, create_time, update_time)
select 'dept-it', '信息技术部', null, current_timestamp, current_timestamp
where not exists (select 1 from sys_dept where dept_id = 'dept-it');

insert into sys_user (user_id, username, password_hash, display_name, dept_id, role_id, email, enabled, create_time, update_time)
select 'user-admin', 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', 'dept-it', 'role-super', null, true, current_timestamp, current_timestamp
where not exists (select 1 from sys_user where user_id = 'user-admin');

