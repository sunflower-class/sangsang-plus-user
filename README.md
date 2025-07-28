# User Service - Spring Boot REST API

A RESTful user management service built with Spring Boot and deployed on Azure Kubernetes Service. This service handles user authentication, profile management, and integrates with external authentication providers through an API Gateway.

## Features

- 👤 User Management (CRUD operations)  
- 🔐 OAuth2 Provider Support (Google)
- 🐘 Azure PostgreSQL Database
- 🚀 Kubernetes Deployment
- 🔑 Role-based Access Control
- 📧 Email Verification Workflow
- 🎯 Event-Driven Architecture (Kafka/Azure Event Hubs)
- 🛡️ Spring Security Integration

## Prerequisites

- Java 11+
- Maven 3.6+
- Docker
- kubectl
- Azure CLI (for deployment)

## Environment Variables

The service requires the following environment variables (managed via Kubernetes Secrets):

```bash
# Database Configuration (Azure PostgreSQL)
DATABASE_URL=jdbc:postgresql://your-server.postgres.database.azure.com:5432/user_db?sslmode=require
DATABASE_USERNAME=your-username
DATABASE_PASSWORD=your-password

# Encryption Key (for AES-256)
ENCRYPTION_KEY=your-base64-encoded-key

# Azure Event Hubs Configuration
AZURE_EVENTHUBS_CONNECTION_STRING=your-connection-string
```

**Note**: OAuth2 authentication is handled by the API Gateway, not directly by this service.

## Local Development

```bash
# Clone repository
git clone <your-repo-url>
cd user-service

# Install dependencies
mvn clean install

# Run PostgreSQL with Docker Compose
docker-compose up -d postgres

# Run application
mvn spring-boot:run
```

## API Endpoints

### Public Endpoints (No Authentication Required)
- `GET /api/users/health` - Service health status
- `POST /api/users` - Create new user (Registration)
- `POST /api/users/authenticate` - Authenticate user credentials (Login)
- `POST /api/users/oauth2` - Create/update OAuth2 user (called by Gateway after OAuth2 flow)
- `PUT /api/users/verify-email` - Mark user's email as verified (called by Gateway after KeyCloak verification)

### Authenticated Endpoints (Requires Valid JWT from Gateway)
- `GET /api/users` - List all users with pagination
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update user profile
- `DELETE /api/users/{id}` - Delete user account

### Admin-Only Endpoints (Requires ADMIN role)
- `PUT /api/users/{id}/suspend` - Suspend a user account
- `PUT /api/users/{id}/activate` - Activate a suspended user account
- `POST /api/users/test-event` - Test event publishing (for debugging)

### Service Integration Endpoints

#### OAuth2 User Creation/Login
```bash
POST /api/users/oauth2
Content-Type: application/json

{
  "email": "user@gmail.com",
  "name": "User Name",
  "provider": "GOOGLE"
}
```
**Response**: User profile data
**Note**: Called by Gateway after successful OAuth2 authentication. Creates new user or logs in existing user.

#### Email Verification
```bash
PUT /api/users/verify-email
Content-Type: application/json

{
  "email": "user@example.com"
}
```
**Response**: 
```json
{
  "message": "Email verified successfully",
  "email": "user@example.com"
}
```
**Note**: Called by Gateway after KeyCloak email verification success.

## API Testing for Gateway Integration

### Health Check
```bash
GET http://user-service.user-service:80/api/health
```
**Response:**
```json
{
  "status": "OK",
  "service": "User Service"
}
```

### Get All Users (with pagination)
```bash
GET http://user-service.user-service:80/api/users?page=0&size=10
```
**Response:**
```json
[
  {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "profilePicture": null
  }
]
```

### Get User by ID
```bash
GET http://user-service.user-service:80/api/users/1
```
**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "provider": "LOCAL"
}
```

### Get User by Email
```bash
GET http://user-service.user-service:80/api/users/email/user@example.com
```

### Create User
```bash
POST http://user-service.user-service:80/api/users
Content-Type: application/json

{
  "email": "newuser@example.com",
  "name": "New User",
  "password": "password123"
}
```
**Response:**
```json
{
  "id": 2,
  "email": "newuser@example.com",
  "name": "New User",
  "role": "USER",
  "provider": "LOCAL",
  "profilePicture": null
}
```

### Update User
```bash
PUT http://user-service.user-service:80/api/users/1
Content-Type: application/json

{
  "name": "Updated Name",
  "email": "updated@example.com"
}
```

### Delete User
```bash
DELETE http://user-service.user-service:80/api/users/1
```
**Response:**
```json
{
  "message": "User deleted successfully"
}
```

### Authenticate User
```bash
POST http://user-service.user-service:80/api/users/authenticate
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```
**Response (Success):**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "provider": "LOCAL",
  "profilePicture": null
}
```
**Response (Error):**
```json
{
  "error": "Invalid credentials"
}
```

### Create OAuth2 User
```bash
POST http://user-service.user-service:80/api/users/oauth2
Content-Type: application/json

{
  "email": "oauth.user@gmail.com",
  "name": "OAuth User",
  "provider": "GOOGLE"
}
```
**Response:**
```json
{
  "id": 3,
  "email": "oauth.user@gmail.com",
  "name": "OAuth User",
  "role": "USER",
  "provider": "GOOGLE",
  "profilePicture": null
}
```

### Gateway Integration Example
For API Gateway routing configuration:
```yaml
# Example gateway route
routes:
  - id: user-service
    uri: http://user-service.user-service:80
    predicates:
      - Path=/api/users/**
    filters:
      - StripPrefix=0
```

## Docker Deployment

```bash
# Build application
mvn clean package

# Build Docker image
docker build -t user-service:latest .

# Run with Docker Compose
docker-compose up
```

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (AKS recommended)
- kubectl configured with proper permissions
- Docker image: `buildingbite/sangsangplus-user:latest`  
- Azure PostgreSQL server already provisioned
- Azure Event Hubs connection string

### Required Secrets

Before deployment, create the following Kubernetes secrets:

```bash
# Azure PostgreSQL connection
kubectl create secret generic azure-postgres-secret \
  --from-literal=DATABASE_URL="jdbc:postgresql://your-server.postgres.database.azure.com:5432/user_db?sslmode=require" \
  --from-literal=DATABASE_USERNAME="your-username" \
  --from-literal=DATABASE_PASSWORD="your-password" \
  -n user-service

# Encryption key for AES-256
kubectl create secret generic encryption-secret \
  --from-literal=encryption.key="your-base64-encoded-key" \
  -n user-service
```

### Deployment Steps

1. **Create Namespace**
   ```bash
   kubectl create namespace user-service
   ```

2. **Setup Azure PostgreSQL Database**
   ```bash
   # Run the setup script to create database
   ./scripts/setup-azure-db.sh
   ```

3. **Deploy Secrets** (if not created above)
   ```bash
   kubectl apply -f k8s/azure-postgres-secret.yaml
   kubectl apply -f k8s/encryption-secret.yaml
   ```

4. **Create ConfigMap**
   ```bash
   kubectl apply -f k8s/configmap.yaml
   ```

5. **Deploy User Service**
   ```bash
   kubectl apply -f k8s-deployment.yaml
   ```

5. **Create Service** (ClusterIP for internal access)
   ```bash
   kubectl apply -f k8s/service.yaml
   ```

### All-in-One Deployment
```bash
# Deploy all resources at once
kubectl apply -f k8s/

# Check deployment status
kubectl get all -n user-service

# Check pod logs
kubectl logs -f deployment/user-service -n user-service
```

### Service Access

The User Service is deployed as ClusterIP and accessible within the cluster at:
```
http://user-service.user-service.svc.cluster.local:8081
```

For API Gateway integration:
```yaml
# Gateway environment variable
- name: USER_SERVICE_URL
  value: "http://user-service.user-service.svc.cluster.local:8081"
```

### Cross-Cluster Deployment Considerations

When deploying in different clusters or environments:

1. **Database Configuration**
   - Update `DATABASE_URL` in secrets to point to your Azure PostgreSQL server
   - Ensure database `user_db` exists on your PostgreSQL server
   - Verify SSL connection requirements (`sslmode=require`)

2. **Network Connectivity**
   - Ensure Kubernetes cluster can reach Azure PostgreSQL (firewall rules)
   - For private endpoints, configure VNet peering or private DNS zones

3. **Event Hubs Configuration**
   - Update `AZURE_EVENTHUBS_CONNECTION_STRING` in ConfigMap
   - Ensure Event Hubs topic exists for user events

4. **Image Registry Access**
   - Ensure cluster can pull from `buildingbite/sangsangplus-user:latest`
   - Or push image to your own registry and update deployment

5. **Service Discovery**
   - Update Gateway service URL to match your namespace/cluster FQDN
   - Consider using Ingress for external access if needed

### Verify Deployment

```bash
# Check all resources in user-service namespace
kubectl get all -n user-service

# Check service endpoints
kubectl get endpoints -n user-service

# Test connectivity from another pod
kubectl run test-pod --image=curlimages/curl -it --rm -- /bin/sh
# Inside the pod:
curl http://user-service.user-service:80/api/health
```

## Production Configuration

For production deployment:

1. Use Kubernetes Secrets for sensitive data:
```bash
kubectl create secret generic user-service-secrets \
  --from-literal=google-client-secret=your-secret \
  --from-literal=jwt-secret=your-jwt-secret \
  --from-literal=db-password=your-db-password \
  -n user-service
```

2. Update ConfigMap to reference secrets
3. Use proper SSL certificates
4. Configure ingress with proper domains
5. Set up monitoring and logging

## Security Notes

### Authentication & Authorization

The User Service implements endpoint-level security with the following rules:

**Public Endpoints** (No authentication required):
- Health check
- User registration (`POST /api/users`)
- User login (`POST /api/users/authenticate`)
- OAuth2 user creation (`POST /api/users/oauth2`)

**Authenticated Endpoints** (Requires valid JWT token):
- User profile operations (GET, PUT, DELETE)
- User search operations

**Admin-Only Endpoints** (Requires ADMIN role):
- User suspension/activation
- Test event publishing

**Architecture Note**: This service operates in a microservices architecture where:
- **API Gateway** handles OAuth2 authentication, JWT validation, and request routing
- **User Service** manages user data and provides endpoints for Gateway integration
- **KeyCloak** (external) handles email verification workflow
- Authentication flow: Client → Gateway → User Service

### Security Best Practices

⚠️ **Important**: Never commit sensitive information to Git:
- `client-secret.json` files
- Database passwords
- JWT secrets
- SSL private keys
- Environment files with secrets

All sensitive data should be:
- Added to `.gitignore`
- Stored in environment variables
- Managed via Kubernetes Secrets in production

## Security Roadmap

### 🔄 Current Implementation
- ✅ JWT Authentication with HttpOnly Cookies
- ✅ OAuth2 Integration (Google)
- ✅ CSRF Protection
- ✅ Role-based Access Control
- ✅ Environment-based Configuration
- ✅ Password Hashing (BCrypt)

### 🚧 Planned Security Enhancements

#### High Priority
- [ ] **Password Strength Validation**
  - Minimum 8 characters with complexity requirements
  - Password history tracking
  - Common password blacklist validation
  
- [ ] **Account Security**
  - Account lockout after failed login attempts
  - Login attempt rate limiting
  - Suspicious activity detection
  
- [ ] **Token Management**
  - Token blacklisting for logout
  - Refresh token rotation
  - Token revocation endpoint

#### Medium Priority  
- [ ] **API Rate Limiting**
  - Request rate limiting per IP/user
  - Brute force attack prevention
  - API quota management
  
- [ ] **Audit & Monitoring**
  - Authentication event logging
  - Failed login attempt tracking
  - User activity audit trail
  
- [ ] **Advanced Security**
  - Multi-factor authentication (MFA)
  - Email verification workflow
  - Password reset functionality

#### Low Priority
- [ ] **Session Management**
  - Concurrent session limiting
  - Session timeout configuration
  - Active session monitoring
  
- [ ] **Data Protection**
  - Data encryption at rest
  - PII data masking in logs
  - GDPR compliance features

### 🛠️ Implementation Notes

**Password Strength Validation**
```java
// Planned implementation location: 
// src/main/java/com/example/userservice/validation/PasswordValidator.java
```

**Account Lockout**
```java
// Planned implementation location:
// src/main/java/com/example/userservice/security/AccountLockoutService.java
```

**Rate Limiting**
```java
// Planned implementation location:
// src/main/java/com/example/userservice/security/RateLimitingFilter.java
```

**Token Blacklist**
```java
// Planned implementation location:
// src/main/java/com/example/userservice/security/TokenBlacklistService.java
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.