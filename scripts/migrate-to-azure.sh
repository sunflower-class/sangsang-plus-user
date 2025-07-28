#!/bin/bash

# 데이터 마이그레이션 스크립트

echo "=== User Service PostgreSQL to Azure Migration ==="

# 1. K8s PostgreSQL에서 데이터 덤프
echo "1. Backing up data from K8s PostgreSQL..."
kubectl exec -n user-service deployment/postgres -- pg_dump -U postgres -d userdb > userdb_backup.sql

# 2. Azure PostgreSQL에 데이터베이스 생성
echo "2. Creating database in Azure PostgreSQL..."
PGPASSWORD='ekdma1443!' psql -h sangsangplus-postgre-server.postgres.database.azure.com \
  -U sangsangplus_admin \
  -d postgres \
  -p 5432 \
  --set=sslmode=require \
  -c "CREATE DATABASE user_db;"

# 3. 백업 데이터를 Azure PostgreSQL로 복원
echo "3. Restoring data to Azure PostgreSQL..."
PGPASSWORD='ekdma1443!' psql -h sangsangplus-postgre-server.postgres.database.azure.com \
  -U sangsangplus_admin \
  -d user_db \
  -p 5432 \
  --set=sslmode=require \
  < userdb_backup.sql

echo "Migration completed!"