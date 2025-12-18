#!/bin/bash

# 数据库配置
DB_HOST="212.227.166.131"
DB_PORT="9257"
DB_USER="hobart"
DB_PASS="123456"

# 颜色输出
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================="
echo "为测试用户初始化余额"
echo "========================================="

# 用户列表
users=("user1" "user2" "user3" "user4")

# Java hashCode 算法（简化版，用于演示）
# user1 -> 111267105 % 4 = 1 -> myapp_1
# user2 -> 111267106 % 4 = 2 -> myapp_2  
# user3 -> 111267107 % 4 = 3 -> myapp_3
# user4 -> 111267108 % 4 = 0 -> myapp_0

declare -A user_db_map
user_db_map["user1"]="myapp_1"
user_db_map["user2"]="myapp_2"
user_db_map["user3"]="myapp_3"
user_db_map["user4"]="myapp_0"

for user in "${users[@]}"
do
    db_name="${user_db_map[$user]}"
    echo ""
    echo -e "${BLUE}正在为 $user 充值（数据库: $db_name）${NC}"
    echo "-------------------------------------------"
    
    # 创建临时SQL文件
    temp_sql=$(mktemp)
    cat > "$temp_sql" << EOF
DELETE FROM balances WHERE user_id = '$user';

INSERT INTO balances (user_id, currency, available, frozen) VALUES
('$user', 'BTC', 100.00000000, 0.00000000),
('$user', 'USDT', 1000000.00000000, 0.00000000),
('$user', 'ETH', 1000.00000000, 0.00000000);

SELECT CONCAT('✓ ', user_id, ' 余额初始化成功') AS status FROM balances WHERE user_id = '$user' LIMIT 1;
EOF
    
    # 执行SQL
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $db_name < "$temp_sql" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $user 充值成功${NC}"
    else
        echo -e "${YELLOW}✗ $user 充值失败（可能需要检查数据库连接）${NC}"
    fi
    
    # 删除临时文件
    rm "$temp_sql"
done

echo ""
echo "========================================="
echo -e "${GREEN}所有用户余额初始化完成！${NC}"
echo "========================================="
echo ""
echo "初始余额："
echo "  - BTC: 100"
echo "  - USDT: 1,000,000"
echo "  - ETH: 1,000"
