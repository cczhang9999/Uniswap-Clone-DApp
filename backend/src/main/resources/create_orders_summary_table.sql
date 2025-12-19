-- Summary table for orders (Non-sharded, for analytics/reporting)
CREATE TABLE IF NOT EXISTS orders_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    symbol VARCHAR(20),
    side VARCHAR(10),
    type VARCHAR(10),
    price DECIMAL(20, 8),
    quantity DECIMAL(20, 8),
    status VARCHAR(20),
    filled_quantity DECIMAL(20, 8) DEFAULT 0,
    timestamp BIGINT,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_timestamp (timestamp)
);
