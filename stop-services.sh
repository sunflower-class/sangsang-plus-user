#!/bin/bash

echo "Stopping user service..."
docker-compose down

echo "Stopping infrastructure services..."
docker-compose -f infra/docker-compose.infra.yml down

echo "All services stopped!"

# 네트워크 제거 여부 확인
read -p "Do you want to remove the sangsang-plus network? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    docker network rm sangsang-plus
    echo "Network removed."
fi