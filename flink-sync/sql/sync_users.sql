-- Enable checkpointing for CDC
SET 'execution.checkpointing.interval' = '10s';

-- 1. Define Source Table (MySQL CDC)
-- Reads from the physical database 'ds0'. 
-- Matches tables 'users_0', 'users_1', 'users_2' via regex.
CREATE TABLE source_users (
    id BIGINT,
    name STRING,
    email STRING,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = '212.227.166.131',
    'port' = '9257',
    'username' = 'hobart',
    'password' = '123456',
    'database-name' = 'myapp',
    'table-name' = 'users_.*', -- Regex to match sharded tables
    'server-time-zone' = 'UTC'
);

-- 2. Define Sink Table (JDBC to Target MySQL)
CREATE TABLE sink_users (
    id BIGINT,
    name STRING,
    email STRING,
    PRIMARY KEY (id) NOT ENFORCED
) WITH (
    'connector' = 'jdbc',
    'url' = 'jdbc:mysql://mysql_target:3306/target_db',
    'username' = 'target_user',
    'password' = 'target_password',
    'table-name' = 'users'
);

-- 3. Sync Logic
-- Simple Insert-Select to sync all matching records
INSERT INTO sink_users SELECT id, name, email FROM source_users;
