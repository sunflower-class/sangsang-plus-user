#!/bin/bash

echo "=== Setting up Azure PostgreSQL for User Service ==="

# 1. Azure PostgreSQL에 데이터베이스 생성 (이미 없는 경우에만)
echo "1. Creating database in Azure PostgreSQL (if not exists)..."
PGPASSWORD='ekdma1443!' psql -h sangsangplus-postgre-server.postgres.database.azure.com \
  -U sangsangplus_admin \
  -d postgres \
  -p 5432 \
  --set=sslmode=require \
  -t -c "SELECT 1 FROM pg_database WHERE datname = 'user_db'" | grep -q 1 || \
PGPASSWORD='ekdma1443!' psql -h sangsangplus-postgre-server.postgres.database.azure.com \
  -U sangsangplus_admin \
  -d postgres \
  -p 5432 \
  --set=sslmode=require \
  -c "CREATE DATABASE user_db;"

echo "Database setup completed!"
echo ""
echo "To migrate data from existing PostgreSQL, run:"
echo "./scripts/migrate-to-azure.sh"