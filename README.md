# Employee Management System

A microservices-based Employee Management System built with Spring Boot, Docker, and role-based access control.

## 🏗️ System Architecture

This system implements a complete microservices architecture with:

- **Service Discovery** (Eureka)
- **API Gateway** (Spring Cloud Gateway)
- **Config Server** with Private Configuration Repository
- **Authentication Service** (JWT + RBAC)
- **Employee Management Service** (Reactive)
- **Notification Service** (Event-driven with Kafka)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker Desktop
- Maven 3.6+

### 1. Clone & Setup
```bash
git clone https://github.com/yourusername/employee-management-system.git
cd employee-management-system

docker-compose up -d postgres-auth postgres-employee postgres-notification zookeeper kafka

# Terminal 1 - Discovery Server
cd discovery-server && mvn spring-boot:run

# Terminal 2 - Config Server  
cd config-server && mvn spring-boot:run

# Terminal 3 - Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 4 - Employee Service
cd employee-service && mvn spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway && mvn spring-boot:run

Open: http://localhost:8761 - All services should be registered!

🔧 Configuration Management
Configuration is managed through a private repository for security. The interview panel has been granted access to:

https://github.com/yourusername/employee-management-config (PRIVATE)

This contains:

Environment-specific configurations

Database credentials

JWT secrets

Service routing rules

Kafka config

🐳 Docker Deployment
bash
# Full Docker deployment
docker-compose up --build -d

# Check service status
docker-compose ps

📡 API Endpoints
Authentication
http
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
Employee Management
