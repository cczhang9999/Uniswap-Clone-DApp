CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS users_0 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users_1 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS users_2 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    currency VARCHAR(50),
    amount DECIMAL(20, 8),
    timestamp BIGINT
);

CREATE TABLE IF NOT EXISTS balances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    currency VARCHAR(50),
    available DECIMAL(20, 8),
    frozen DECIMAL(20, 8)
);

CREATE TABLE IF NOT EXISTS orders (
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
    timestamp BIGINT
);
