create table transfers(
    id uuid primary key,
    file_name varchar(255) not null,
    created_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    download_token varchar(255) not null,
    constraint uk_transfers_download_token unique(download_token),
    blob_name varchar(255) not null
)