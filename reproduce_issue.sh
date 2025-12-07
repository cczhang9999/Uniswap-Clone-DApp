#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "1. Depositing funds for Seller (user1)..."
curl -s -X POST $BASE_URL/balance/deposit -H "Content-Type: application/json" -d '{
    "userId": "user1",
    "currency": "BTC",
    "amount": 10
}'
echo -e "\n"

echo "2. Depositing funds for Buyer (user2)..."
curl -s -X POST $BASE_URL/balance/deposit -H "Content-Type: application/json" -d '{
    "userId": "user2",
    "currency": "USDT",
    "amount": 100000
}'
echo -e "\n"

echo "3. Placing SELL Order: 1 BTC @ 50000 (user1)..."
curl -s -X POST $BASE_URL/order -H "Content-Type: application/json" -d '{
    "userId": "user1",
    "symbol": "BTC-USDT",
    "side": "SELL",
    "type": "LIMIT",
    "price": 50000,
    "quantity": 1
}'
echo -e "\n"

echo "4. Placing BUY Order: 1 BTC @ 50000 (user2)..."
curl -s -X POST $BASE_URL/order -H "Content-Type: application/json" -d '{
    "userId": "user2",
    "symbol": "BTC-USDT",
    "side": "BUY",
    "type": "LIMIT",
    "price": 50000,
    "quantity": 1
}'
echo -e "\n"

echo "5. Verifying Order History for user1..."
curl -s -X GET $BASE_URL/orders/user1 -H "Content-Type: application/json"
echo -e "\n"

echo "Done."
