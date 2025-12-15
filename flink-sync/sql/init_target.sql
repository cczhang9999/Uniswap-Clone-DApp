CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY,
    order_id VARCHAR(255),
    user_id VARCHAR(255),
    symbol VARCHAR(20),
    side VARCHAR(10),
    type VARCHAR(10),
    price DECIMAL(20, 8),
    quantity DECIMAL(20, 8),
    status VARCHAR(20),
    filled_quantity DECIMAL(20, 8),
    timestamp BIGINT
);
