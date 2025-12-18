#!/bin/bash

# 后端API地址
API_URL="http://localhost:8080/api/v1/order/balance/deposit"

# 颜色输出
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "========================================="
echo "通过 API 为测试用户充值"
echo "========================================="

# 用户列表
users=("user1" "user2" "user3" "user4")
currencies=("BTC" "USDT" "ETH")

# 充值金额
declare -A amounts
amounts["BTC"]="100"
amounts["USDT"]="1000000"
amounts["ETH"]="1000"

for user in "${users[@]}"
do
    echo ""
    echo -e "${BLUE}正在为 $user 充值...${NC}"
    echo "-------------------------------------------"
    
    for currency in "${currencies[@]}"
    do
        amount="${amounts[$currency]}"
        
        # 调用 API
        response=$(curl -s -X POST "$API_URL" \
            -H "Content-Type: application/json" \
            -d "{\"userId\":\"$user\",\"currency\":\"$currency\",\"amount\":$amount}")
        
        # 检查响应
        if echo "$response" | grep -q "success"; then
            echo -e "${GREEN}✓ $user - $currency: $amount${NC}"
        else
            echo "✗ $user - $currency: 失败 ($response)"
        fi
        
        # 避免请求过快
        sleep 0.1
    done
done

echo ""
echo "========================================="
echo -e "${GREEN}充值完成！${NC}"
echo "========================================="
