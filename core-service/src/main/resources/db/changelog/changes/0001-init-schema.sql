CREATE SCHEMA IF NOT EXISTS uplink;

CREATE TABLE IF NOT EXISTS uplink.liquibase_smoke
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    created_at
    TIMESTAMPTZ
    NOT
    NULL
    DEFAULT
    now
(
)
    );
