create table transfers(
    id uuid primary key,
    fileName varchar(255) not null,
    createdAt timestamp with time zone not null,
    expiresAt timestamp with time zone not null,
    downloadToken varchar(255) not null,
    blobName varchar(255) not null
)