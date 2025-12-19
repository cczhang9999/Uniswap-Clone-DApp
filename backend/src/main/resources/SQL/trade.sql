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
