alter table transfers
    add column status varchar(50) not null default 'READY';