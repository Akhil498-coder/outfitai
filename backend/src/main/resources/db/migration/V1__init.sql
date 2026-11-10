-- Baseline migration: proves Flyway runs on both H2 and PostgreSQL.
-- Real tables (users, clothing items, ...) arrive in later phases as V2, V3, ...
CREATE TABLE app_info (
    info_key   VARCHAR(100) PRIMARY KEY,
    info_value VARCHAR(255) NOT NULL
);

INSERT INTO app_info (info_key, info_value) VALUES ('schema_baseline', 'phase-1');
