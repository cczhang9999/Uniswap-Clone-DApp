#!/bin/bash

# 数据库配置
DB_HOST="212.227.166.131"
DB_PORT="9257"
DB_USER="hobart"
DB_PASS="123456"
SQL_FILE="backend/src/main/resources/init_all_databases.sql"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "========================================="
echo "开始初始化 ShardingSphere 数据库"
echo "========================================="

# 检查 SQL 文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo -e "${RED}错误: SQL 文件不存在: $SQL_FILE${NC}"
    exit 1
fi

# 循环执行 4 个数据库
for i in {0..3}
do
    DB_NAME="myapp_$i"
    echo ""
    echo -e "${GREEN}正在初始化数据库: $DB_NAME${NC}"
    echo "-------------------------------------------"
    
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < $SQL_FILE
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $DB_NAME 初始化成功${NC}"
    else
        echo -e "${RED}✗ $DB_NAME 初始化失败${NC}"
        exit 1
    fi
done

echo ""
echo "========================================="
echo -e "${GREEN}所有数据库初始化完成！${NC}"
echo "========================================="
echo ""
echo "请重启后端服务: ./run_backend.sh"
