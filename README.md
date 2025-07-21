# User Service - Spring Boot REST API

A RESTful user management service with Google OAuth2 authentication, built with Spring Boot and deployed on Azure Kubernetes Service.

## Features

- 🔐 Google OAuth2 Authentication
- 👤 User Management (CRUD operations)  
- 🐘 PostgreSQL Database
- 🚀 Kubernetes Deployment
- 🔒 JWT Token Support (HttpOnly Cookies)
- ☁️ Azure Cloud Integration
- 🛡️ CSRF Protection
- 🔑 Role-based Access Control
- 📱 Environment-based Configuration (.env)

## Prerequisites

- Java 11+
- Maven 3.6+
- Docker
- kubectl
- Azure CLI (for deployment)

## Environment Variables

Create a `.env` file or set these environment variables:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/userdb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-db-password

# Google OAuth2 Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH_REDIRECT_URI=https://your-domain.com/login/oauth2/code/google

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=2592000000
```

## Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Download `client-secret.json` and place it in `src/main/resources/`
6. Add your domain to authorized redirect URIs

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

### Health Check
- `GET /api/health` - Service health status

### User Management
- `GET /api/users` - List all users with pagination
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `POST /api/users/authenticate` - Authenticate user credentials

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

- Kubernetes cluster (AKS, EKS, GKE, or local minikube)
- kubectl configured
- Docker image built: `buildingbite/sangsangplus-user:latest`
- Kafka service deployed (as `kafka-service` in the cluster)

### Deployment Steps

Deploy in the following order:

1. **Create Namespace**
   ```bash
   kubectl apply -f k8s/namespace.yaml
   ```

2. **Deploy PostgreSQL Database**
   ```bash
   kubectl apply -f k8s/postgres.yaml
   ```

3. **Create ConfigMap** (Application Configuration)
   ```bash
   kubectl apply -f k8s/configmap.yaml
   ```

4. **Deploy User Service**
   ```bash
   kubectl apply -f k8s/deployment.yaml
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
http://user-service.user-service:80
```

For API Gateway integration (from default namespace):
```yaml
# Gateway environment variable
- name: USER_SERVICE_URL
  value: "http://user-service.user-service:80"
```

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