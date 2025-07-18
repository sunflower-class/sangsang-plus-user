#!/bin/bash

# Azure Container Registry 정보
ACR_NAME="testregistryforuser"
RESOURCE_GROUP="test"
STATIC_IP="20.249.144.238"
STATIC_IP_NAME="testip"

# Docker 이미지 빌드
echo "Building Docker image..."
docker build -t user-service:latest .

# Azure Container Registry 로그인
echo "Logging into Azure Container Registry..."
az acr login --name $ACR_NAME

# 이미지 태그 및 푸시
echo "Tagging and pushing image..."
docker tag user-service:latest $ACR_NAME.azurecr.io/user-service:latest
docker push $ACR_NAME.azurecr.io/user-service:latest

# Kubernetes 파일 업데이트
echo "Updating Kubernetes files..."
sed -i "s/your-registry/$ACR_NAME/g" k8s/deployment.yaml
sed -i "s/your-resource-group/$RESOURCE_GROUP/g" k8s/service.yaml
sed -i "s/your-static-ip-name/$STATIC_IP_NAME/g" k8s/service.yaml
sed -i "s/YOUR_STATIC_IP_HERE/$STATIC_IP/g" k8s/service.yaml

# Kubernetes에 배포
echo "Deploying to Kubernetes..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 배포 상태 확인
echo "Checking deployment status..."
kubectl get pods -n user-service
kubectl get svc -n user-service

echo "Deployment complete!"
echo "Your service will be available at: http://$STATIC_IP"