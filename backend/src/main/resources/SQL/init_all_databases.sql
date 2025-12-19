-- ============================================
-- 完整的数据库初始化脚本
-- 需要在 myapp_0, myapp_1, myapp_2, myapp_3 四个数据库中分别执行
-- ============================================

-- 1. 创建 balances 表（余额表）
DROP TABLE IF EXISTS balances;
CREATE TABLE balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    available DECIMAL(20, 8) NOT NULL DEFAULT 0,
    frozen DECIMAL(20, 8) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_currency (user_id, currency),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户余额表';

-- 1.5 创建 deposits 表（充值记录表）
DROP TABLE IF EXISTS deposits;
CREATE TABLE deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    amount DECIMAL(20, 8) NOT NULL,
    timestamp BIGINT NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- 2. 创建 t_order 分片表（16张表）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表分片0';

DROP TABLE IF EXISTS t_order_1;
CREATE TABLE t_order_1 LIKE t_order_0;
ALTER TABLE t_order_1 COMMENT='订单表分片1';

DROP TABLE IF EXISTS t_order_2;
CREATE TABLE t_order_2 LIKE t_order_0;
ALTER TABLE t_order_2 COMMENT='订单表分片2';

DROP TABLE IF EXISTS t_order_3;
CREATE TABLE t_order_3 LIKE t_order_0;
ALTER TABLE t_order_3 COMMENT='订单表分片3';

DROP TABLE IF EXISTS t_order_4;
CREATE TABLE t_order_4 LIKE t_order_0;
ALTER TABLE t_order_4 COMMENT='订单表分片4';

DROP TABLE IF EXISTS t_order_5;
CREATE TABLE t_order_5 LIKE t_order_0;
ALTER TABLE t_order_5 COMMENT='订单表分片5';

DROP TABLE IF EXISTS t_order_6;
CREATE TABLE t_order_6 LIKE t_order_0;
ALTER TABLE t_order_6 COMMENT='订单表分片6';

DROP TABLE IF EXISTS t_order_7;
CREATE TABLE t_order_7 LIKE t_order_0;
ALTER TABLE t_order_7 COMMENT='订单表分片7';

DROP TABLE IF EXISTS t_order_8;
CREATE TABLE t_order_8 LIKE t_order_0;
ALTER TABLE t_order_8 COMMENT='订单表分片8';

DROP TABLE IF EXISTS t_order_9;
CREATE TABLE t_order_9 LIKE t_order_0;
ALTER TABLE t_order_9 COMMENT='订单表分片9';

DROP TABLE IF EXISTS t_order_10;
CREATE TABLE t_order_10 LIKE t_order_0;
ALTER TABLE t_order_10 COMMENT='订单表分片10';

DROP TABLE IF EXISTS t_order_11;
CREATE TABLE t_order_11 LIKE t_order_0;
ALTER TABLE t_order_11 COMMENT='订单表分片11';

DROP TABLE IF EXISTS t_order_12;
CREATE TABLE t_order_12 LIKE t_order_0;
ALTER TABLE t_order_12 COMMENT='订单表分片12';

DROP TABLE IF EXISTS t_order_13;
CREATE TABLE t_order_13 LIKE t_order_0;
ALTER TABLE t_order_13 COMMENT='订单表分片13';

DROP TABLE IF EXISTS t_order_14;
CREATE TABLE t_order_14 LIKE t_order_0;
ALTER TABLE t_order_14 COMMENT='订单表分片14';

DROP TABLE IF EXISTS t_order_15;
CREATE TABLE t_order_15 LIKE t_order_0;
ALTER TABLE t_order_15 COMMENT='订单表分片15';

-- 3. 插入测试数据（可选）
-- 为 user1 初始化余额
INSERT INTO balances (user_id, currency, available, frozen) 
VALUES ('user1', 'BTC', 10.00000000, 0.00000000)
ON DUPLICATE KEY UPDATE available = 10.00000000;

INSERT INTO balances (user_id, currency, available, frozen) 
VALUES ('user1', 'USDT', 100000.00000000, 0.00000000)
ON DUPLICATE KEY UPDATE available = 100000.00000000;

-- 验证表创建
SELECT 'balances 表创建成功' AS status, COUNT(*) AS row_count FROM balances;
SELECT 't_order_0 表创建成功' AS status FROM t_order_0 LIMIT 0;
SELECT 't_order_15 表创建成功' AS status FROM t_order_15 LIMIT 0;
