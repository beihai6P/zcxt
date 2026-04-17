create table if not exists asset_attachment (
    attachment_id varchar(32) primary key,
    asset_id varchar(32),
    file_name varchar(255) not null,
    original_name varchar(255) not null,
    content_type varchar(100),
    size_bytes bigint not null,
    url varchar(500) not null,
    uploader_id varchar(32),
    upload_time datetime not null
);

create index if not exists idx_asset_attachment_asset_id on asset_attachment (asset_id);

