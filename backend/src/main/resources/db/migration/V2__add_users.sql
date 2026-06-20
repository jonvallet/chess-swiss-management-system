create table users (
    id uuid not null,
    username varchar(255) not null,
    password varchar(255) not null,
    role varchar(50) not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    primary key (id)
);

alter table if exists users
    add constraint UK_users_username unique (username);
