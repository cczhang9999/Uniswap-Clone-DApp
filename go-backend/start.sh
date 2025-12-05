#!/bin/bash

# Ensure we are in the directory where the script is located
cd "$(dirname "$0")"

echo "Checking dependencies..."
go mod tidy

echo "Starting Go Backend..."
go run main.go
