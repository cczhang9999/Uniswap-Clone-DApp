# Flink SQL Sync Demo

This project demonstrates how to synchronize data from a remote sharded MySQL database to a local MySQL target using Flink SQL and CDC.

## Prerequisites
- Docker & Docker Compose
- Internet connection (to download JARs and connect to remote source)

## Setup

1. **Download Dependencies**:
   Run the helper script to download the required Flink connector JARs.
   ```bash
   cd flink-sync
   sh download_jars.sh
   ```

2. **Start Infrastructure**:
   Start the Flink Cluster (JobManager + TaskManager) and the Target MySQL database.
   ```bash
   docker-compose up -d
   ```

3. **Initialize Target Database**:
   Connect to the local target database to create the table.
   ```bash
   # Password is 'target_password'
   docker exec -it flink-sync-mysql_target-1 mysql -utarget_user -p target_db < sql/init_target.sql
   ```
   *Note: Container name might vary (`flink-sync_mysql_target_1` vs `flink-sync-mysql_target-1`), check `docker ps`.*

## Run Synchronization

1. **Submit Flink Job**:
   You can submit the SQL job using the Flink SQL Client (if installed) or via the Flink Web UI (http://localhost:8081).
   
   **Option A: Via Web UI**
   - Go to http://localhost:8081
   - Click "Submit New Job" (This is for JARs, for SQL you typically use SQL Client).
   
   **Option B: Via SQL Client (Recommended)**
   Since we didn't bundle a separate SQL client container, we can run it inside the JobManager container:
   ```bash
   docker exec -it flink-sync-jobmanager-1 ./bin/sql-client.sh -l /opt/flink/lib_extra -f /opt/flink/sql/sync_users.sql
   ```

2. **Verify Data**:
   Check the target database:
   ```bash
   docker exec -it flink-sync-mysql_target-1 mysql -utarget_user -ptarget_password target_db -e "SELECT * FROM users;"
   ```
