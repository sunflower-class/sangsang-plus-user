-- User 테이블 생성 스크립트 (JPA가 자동 생성하지만, 수동 실행이 필요한 경우 사용)

-- Role 타입 생성
CREATE TYPE role_type AS ENUM ('USER', 'ADMIN');

-- Provider 타입 생성  
CREATE TYPE provider_type AS ENUM ('LOCAL', 'GOOGLE');

-- Users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    provider VARCHAR(20) DEFAULT 'LOCAL',
    email_verified BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    login_count INTEGER DEFAULT 0
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);