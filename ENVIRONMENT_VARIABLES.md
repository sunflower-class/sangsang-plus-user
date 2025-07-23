# User Service 환경변수 설정 가이드

## 필수 환경변수

### 1. Azure Event Hubs 설정
```yaml
# Azure Event Hubs 연결 문자열 (필수)
# Format: Endpoint=sb://[namespace].servicebus.windows.net/;SharedAccessKeyName=[key-name];SharedAccessKey=[key]
AZURE_EVENTHUBS_CONNECTION_STRING: "Endpoint=sb://your-eventhubs-namespace.servicebus.windows.net/;SharedAccessKeyName=your-key-name;SharedAccessKey=your-access-key"

# Kafka Bootstrap Servers (ConfigMap에서 설정)
# Azure Event Hubs의 경우: [namespace].servicebus.windows.net:9093
spring.kafka.bootstrap-servers: "your-eventhubs-namespace.servicebus.windows.net:9093"
```

### 2. 데이터베이스 설정
```yaml
# PostgreSQL 연결 정보
DATABASE_URL: "jdbc:postgresql://postgres-service:5432/userdb"
DATABASE_USERNAME: "postgres"
DATABASE_PASSWORD: "your-secure-password"
```

### 3. 보안 설정
```yaml
# AES-256 암호화 키 (64자 Hex String)
ENCRYPTION_KEY: "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
```

## Azure Event Hubs 설정 단계

### 1. Azure Portal에서 Event Hubs 생성
1. Event Hubs Namespace 생성
2. `user-events` Event Hub(토픽) 생성
3. Shared Access Policy 생성 (Send 권한 필요)

### 2. 연결 문자열 획득
1. Azure Portal > Event Hubs Namespace > Shared access policies
2. 생성한 정책 클릭 > Connection string-primary key 복사
3. **중요**: EntityPath는 제거해야 함

### 3. Kubernetes 설정

#### deployment.yaml에 환경변수 추가:
```yaml
env:
- name: AZURE_EVENTHUBS_CONNECTION_STRING
  value: "your-connection-string-here"
- name: ENCRYPTION_KEY
  valueFrom:
    secretKeyRef:
      name: encryption-secret
      key: encryption.key
```

#### ConfigMap 설정 (configmap.yaml):
```yaml
data:
  application.yml: |
    spring:
      kafka:
        bootstrap-servers: your-namespace.servicebus.windows.net:9093
      datasource:
        url: jdbc:postgresql://postgres-service:5432/userdb
        username: postgres
        password: ${DATABASE_PASSWORD:postgres}
```

## 로컬 개발 환경

로컬에서는 표준 Kafka를 사용할 수 있습니다:
```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
  azure:
    eventhubs:
      connection-string: ""  # 비워두면 표준 Kafka 모드로 동작
```

## 문제 해결

### 연결 실패 시 확인사항:
1. 연결 문자열에 EntityPath가 없는지 확인
2. Event Hubs Namespace가 올바른지 확인
3. Shared Access Key에 Send 권한이 있는지 확인
4. 포트가 9093인지 확인 (표준 Kafka는 9092)

### 로그 확인:
```bash
# Kafka 설정 확인
kubectl logs [pod-name] -n user-service | grep "Kafka Producer configured"

# 이벤트 발행 확인
kubectl logs [pod-name] -n user-service | grep "Successfully published"
```