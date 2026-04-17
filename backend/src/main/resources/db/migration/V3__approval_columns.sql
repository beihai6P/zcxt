alter table asset_approval add column if not exists target_status varchar(20);
alter table asset_approval add column if not exists payload text;

