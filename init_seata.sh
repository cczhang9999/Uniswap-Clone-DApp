#!/bin/bash

# Database Configuration
# Using the same remote DB credentials as sharding.yaml
DB_HOST="212.227.166.131"
DB_PORT="9257"
DB_USER="hobart"
DB_PASS="123456"
SQL_FILE="backend/src/main/resources/create_undo_log.sql"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "========================================="
echo "Initializing Seata 'undo_log' Tables"
echo "========================================="

if [ ! -f "$SQL_FILE" ]; then
    echo -e "${RED}Error: SQL file not found: $SQL_FILE${NC}"
    exit 1
fi

# Loop through all 4 databases
for i in {0..3}
do
    DB_NAME="myapp_$i"
    echo ""
    echo -e "Updating Database: ${GREEN}$DB_NAME${NC}"
    echo "-------------------------------------------"
    
    # Check if mysql command exists
    if ! command -v mysql &> /dev/null; then
        echo -e "${RED}Error: 'mysql' command not found. Please ensure MySQL client is installed.${NC}"
        # Fallback guidance or method could be added here, but for now we exit or warn
        exit 1
    fi

    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < $SQL_FILE
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Success${NC}"
    else
        echo -e "${RED}✗ Failed${NC}"
        exit 1
    fi
done

echo ""
echo "========================================="
echo -e "${GREEN}Seata Initialization Complete!${NC}"
echo "========================================="
