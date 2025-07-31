#!/bin/bash

echo "=== Initializing Azure PostgreSQL Database for User Service ==="

# Azure PostgreSQL 연결 정보
HOST="sangsangplus-postgre-server.postgres.database.azure.com"
USERNAME="sangsangplus_admin"
PASSWORD="ekdma1443!"
DATABASE="user_db"
PORT="5432"

echo "1. Connecting to Azure PostgreSQL..."

# 기존 테이블 삭제 (있다면) 및 새로운 테이블 생성
echo "2. Dropping existing tables and creating new schema..."
PGPASSWORD="$PASSWORD" psql -h "$HOST" \
  -U "$USERNAME" \
  -d "$DATABASE" \
  -p "$PORT" \
  --set=sslmode=require \
  << 'EOF'

-- 기존 테이블 삭제 (cascade로 모든 의존성 포함)
DROP TABLE IF EXISTS users CASCADE;

-- users 테이블 생성 (UUID 기반, 간소화된 구조)
-- Azure PostgreSQL에서는 gen_random_uuid() 사용
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

-- 업데이트 시간 자동 갱신을 위한 함수 생성
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 트리거 생성
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 테이블 정보 확인
\d users

-- 샘플 데이터 삽입 (테스트용)
INSERT INTO users (email, name) VALUES 
    ('admin@buildingbite.com', 'System Admin'),
    ('test@buildingbite.com', 'Test User')
ON CONFLICT (email) DO NOTHING;

-- 결과 확인
SELECT COUNT(*) as total_users FROM users;
SELECT * FROM users LIMIT 5;

EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Database initialization completed successfully!"
    echo ""
    echo "📋 Created:"
    echo "   - users table with UUID primary key"
    echo "   - Email uniqueness constraint"
    echo "   - Indexes for performance"
    echo "   - Auto-update timestamp trigger"
    echo "   - Sample data for testing"
    echo ""
    echo "🔗 Connection details:"
    echo "   Host: $HOST"
    echo "   Database: $DATABASE"
    echo "   Port: $PORT"
else
    echo ""
    echo "❌ Database initialization failed!"
    echo "Please check your connection settings and try again."
    exit 1
fi