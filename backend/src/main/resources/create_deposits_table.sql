-- Create deposits table
-- 需要在所有 4 个数据库 (myapp_0, myapp_1, myapp_2, myapp_3) 中执行

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
