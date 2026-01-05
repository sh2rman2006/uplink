create schema if not exists uplink;

create table if not exists uplink.user_profile
(
    id                uuid primary key,

    username          varchar(32),
    email             varchar(254),
    display_name      varchar(64),
    bio               varchar(280),

    avatar_object_key varchar(512),
    avatar_url        varchar(1024),
    avatar_version    bigint      not null default 1,

    is_profile_public boolean     not null default true,
    status            varchar(16) not null default 'ACTIVE',

    created_at        timestamptz not null default now(),
    updated_at        timestamptz not null default now(),
    last_seen_at      timestamptz
);

create unique index if not exists uk_user_profile_username
    on uplink.user_profile (username)
    where username is not null;

create index if not exists ix_user_profile_created_at
    on uplink.user_profile (created_at);

create index if not exists ix_user_profile_last_seen_at
    on uplink.user_profile (last_seen_at);
