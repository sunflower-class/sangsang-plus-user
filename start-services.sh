#!/bin/bash

# sangsang-plus 네트워크가 없으면 생성
docker network ls | grep -q sangsang-plus || docker network create sangsang-plus

echo "Starting infrastructure services (Kafka, Zookeeper)..."
docker-compose -f infra/docker-compose.infra.yml up -d

# Kafka가 준비될 때까지 대기
echo "Waiting for Kafka to be ready..."
sleep 10

echo "Starting user service and dependencies..."
docker-compose up -d

echo "All services started!"
echo ""
echo "Services running:"
echo "- Zookeeper: localhost:2181"
echo "- Kafka: localhost:9092"
echo "- PostgreSQL: localhost:5432"
echo "- Axon Server: localhost:8024 (GUI), localhost:8124 (gRPC)"
echo "- User Service: localhost:8081"