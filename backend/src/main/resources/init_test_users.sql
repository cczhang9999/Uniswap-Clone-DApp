-- 为测试用户初始化余额
-- 注意：需要根据用户的分片规则在对应的数据库中执行

-- user1 -> ds1 (hashCode % 4 = 1)
-- user2 -> ds2 (hashCode % 4 = 2)
-- user3 -> ds3 (hashCode % 4 = 3)
-- user4 -> ds0 (hashCode % 4 = 0)

-- 在对应的数据库中执行以下语句
-- 例如：在 myapp_1 中为 user1 充值

-- 删除可能存在的旧数据
DELETE FROM balances WHERE user_id IN ('user1', 'user2', 'user3', 'user4');

-- 插入初始余额
INSERT INTO balances (user_id, currency, available, frozen) VALUES
('user1', 'BTC', 100.00000000, 0.00000000),
('user1', 'USDT', 1000000.00000000, 0.00000000),
('user1', 'ETH', 1000.00000000, 0.00000000);

INSERT INTO balances (user_id, currency, available, frozen) VALUES
('user2', 'BTC', 100.00000000, 0.00000000),
('user2', 'USDT', 1000000.00000000, 0.00000000),
('user2', 'ETH', 1000.00000000, 0.00000000);

INSERT INTO balances (user_id, currency, available, frozen) VALUES
('user3', 'BTC', 100.00000000, 0.00000000),
('user3', 'USDT', 1000000.00000000, 0.00000000),
('user3', 'ETH', 1000.00000000, 0.00000000);

INSERT INTO balances (user_id, currency, available, frozen) VALUES
('user4', 'BTC', 100.00000000, 0.00000000),
('user4', 'USDT', 1000000.00000000, 0.00000000),
('user4', 'ETH', 1000.00000000, 0.00000000);

-- 验证插入结果
SELECT user_id, currency, available, frozen FROM balances ORDER BY user_id, currency;
