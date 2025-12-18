-- Create 16 tables for t_order
DROP TABLE IF EXISTS t_order_0;
CREATE TABLE t_order_0 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(32),
    side VARCHAR(16),
    type VARCHAR(16),
    price DECIMAL(20, 8),
    quantity DECIMAL(20, 8),
    status VARCHAR(16),
    filled_quantity DECIMAL(20, 8),
    timestamp BIGINT,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
);
-- Repeat for 1 to 15...
