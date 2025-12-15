#!/bin/bash
mkdir -p lib

# Versions
FLINK_VERSION="1.18.0"
CDC_VERSION="3.0.1"
JDBC_VERSION="3.1.2-1.18"
MYSQL_DRIVER_VERSION="8.2.0"

echo "Downloading Flink SQL Connector MySQL CDC..."
curl -L -o lib/flink-sql-connector-mysql-cdc-${CDC_VERSION}.jar https://repo1.maven.org/maven2/com/ververica/flink-sql-connector-mysql-cdc/${CDC_VERSION}/flink-sql-connector-mysql-cdc-${CDC_VERSION}.jar

echo "Downloading Flink JDBC Connector..."
curl -L -o lib/flink-connector-jdbc-${JDBC_VERSION}.jar https://repo1.maven.org/maven2/org/apache/flink/flink-connector-jdbc/${JDBC_VERSION}/flink-connector-jdbc-${JDBC_VERSION}.jar

echo "Downloading MySQL Driver..."
curl -L -o lib/mysql-connector-j-${MYSQL_DRIVER_VERSION}.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_DRIVER_VERSION}/mysql-connector-j-${MYSQL_DRIVER_VERSION}.jar

echo "Done. JARs are in flink-sync/lib/"
