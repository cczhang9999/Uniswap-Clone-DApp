-- Cleaned up schema.sql for ShardingSphere DDL Routing
-- Ensure this script is run via ShardingSphereDataSource so it routes table creation correctly.

-- Logic Table: users (Mapped to ds0.users_0..2 in sharding.yaml)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

-- Logic Table: deposits (Not sharded or Default DS)
CREATE TABLE IF NOT EXISTS deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    currency VARCHAR(50),
    amount DECIMAL(20, 8),
    timestamp BIGINT
);

-- Logic Table: balances (Not sharded or Default DS)
CREATE TABLE IF NOT EXISTS balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    currency VARCHAR(50),
    available DECIMAL(20, 8),
    frozen DECIMAL(20, 8)
);

-- Logic Table: orders (Mapped to ds0..3.orders_0..3 in sharding.yaml)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Snowflake ID will populate this usually, or order_id
    order_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    symbol VARCHAR(20),
    side VARCHAR(10),
    type VARCHAR(10),
    price DECIMAL(20, 8),
    quantity DECIMAL(20, 8),
    status VARCHAR(20),
    filled_quantity DECIMAL(20, 8) DEFAULT 0,
    timestamp BIGINT
);

-- Logic Table: trade (New table)
CREATE TABLE IF NOT EXISTS trade (
    trade_id VARCHAR(64) NOT NULL COMMENT 'Trade ID',
    buyer_user_id VARCHAR(64) NOT NULL COMMENT 'Buyer User ID',
    seller_user_id VARCHAR(64) NOT NULL COMMENT 'Seller User ID',
    symbol VARCHAR(32) NOT NULL COMMENT 'Trading Pair Symbol (e.g. BTC-USDT)',
    buy_order_id VARCHAR(64) NOT NULL COMMENT 'Buy Order ID',
    sell_order_id VARCHAR(64) NOT NULL COMMENT 'Sell Order ID',
    price DECIMAL(36, 18) NOT NULL COMMENT 'Trade Price',
    quantity DECIMAL(36, 18) NOT NULL COMMENT 'Trade Quantity',
    timestamp BIGINT NOT NULL COMMENT 'Trade Timestamp (ms)',
    PRIMARY KEY (trade_id),
    INDEX idx_buyer_user_id (buyer_user_id),
    INDEX idx_seller_user_id (seller_user_id),
    INDEX idx_symbol_timestamp (symbol, timestamp),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trade History';
