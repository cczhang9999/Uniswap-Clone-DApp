-- Create 16 tables for t_order
-- 使用 order_id (VARCHAR) 作为主键，而不是自增 id
-- 这样可以避免 MyBatis AUTO_INCREMENT 与 ShardingSphere UUID 生成的冲突

DROP TABLE IF EXISTS t_order_0;
CREATE TABLE t_order_0 (
    order_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(32),
    side VARCHAR(16),
    type VARCHAR(16),
    price DECIMAL(20, 8),
    quantity DECIMAL(20, 8),
    status VARCHAR(16),
    filled_quantity DECIMAL(20, 8),
    timestamp BIGINT,
    INDEX idx_user_id (user_id)
);

DROP TABLE IF EXISTS t_order_1;
CREATE TABLE t_order_1 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_2;
CREATE TABLE t_order_2 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_3;
CREATE TABLE t_order_3 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_4;
CREATE TABLE t_order_4 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_5;
CREATE TABLE t_order_5 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_6;
CREATE TABLE t_order_6 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_7;
CREATE TABLE t_order_7 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_8;
CREATE TABLE t_order_8 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_9;
CREATE TABLE t_order_9 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_10;
CREATE TABLE t_order_10 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_11;
CREATE TABLE t_order_11 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_12;
CREATE TABLE t_order_12 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_13;
CREATE TABLE t_order_13 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_14;
CREATE TABLE t_order_14 LIKE t_order_0;

DROP TABLE IF EXISTS t_order_15;
CREATE TABLE t_order_15 LIKE t_order_0;
