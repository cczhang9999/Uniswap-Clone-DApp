-- Create balances table
-- 这个表不分片，但需要在每个数据库 (myapp_0, myapp_1, myapp_2, myapp_3) 中都创建
-- 因为 ShardingSphere 会根据 user_id 路由到不同的数据库

DROP TABLE IF EXISTS balances;
CREATE TABLE balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    available DECIMAL(20, 8) NOT NULL DEFAULT 0,
    frozen DECIMAL(20, 8) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_currency (user_id, currency),
    INDEX idx_user_id (user_id)
);
