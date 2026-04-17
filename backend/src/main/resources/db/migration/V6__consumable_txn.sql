create table if not exists consumable_txn (
    txn_id varchar(32) primary key,
    consumable_id varchar(32) not null,
    txn_type varchar(20) not null,
    quantity int not null,
    dept_id varchar(32),
    user_id varchar(32),
    remark varchar(255),
    txn_time datetime not null
);

create index if not exists idx_consumable_txn_cid on consumable_txn (consumable_id);
create index if not exists idx_consumable_txn_time on consumable_txn (txn_time);

