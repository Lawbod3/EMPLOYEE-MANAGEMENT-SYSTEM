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

### 1. Shared Configuration (application.properties)
```properties
# SHARED CONFIGURATION FOR ALL SERVICES
# =====================================

# SERVICE DISCOVERY / EUREKA
eureka.client.serviceUrl.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}
eureka.instance.preferIpAddress=true

# MANAGEMENT / ACTUATOR
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# LOGGING
logging.level.org.springframework=INFO
logging.level.com.yourcompany=DEBUG

# SECURITY (JWT) - UPDATE THESE SECRETS
jwt.secret=${JWT_SECRET:your-base64-encoded-jwt-secret-here}
jwt.expiration=${JWT_EXPIRATION:86400000}

# REDIS (Rate Limiting & Caching)
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:your-redis-password}
spring.redis.timeout=2000ms

# KAFKA MESSAGE BROKER
spring.kafka.bootstrap-servers=${KAFKA_BROKER:localhost:9092}

# API GATEWAY CONFIGURATION
# =========================

server.port=8080
spring.application.name=api-gateway
spring.main.web-application-type=reactive
spring.cloud.config.enabled=true

# SERVICE DISCOVERY & ROUTING
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true

# AUTH SERVICE ROUTE
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=lb://auth-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1
spring.cloud.gateway.routes[0].filters[1].name=RequestRateLimiter
spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.replenishRate=10
spring.cloud.gateway.routes[0].filters[1].args.redis-rate-limiter.burstCapacity=20
spring.cloud.gateway.routes[0].filters[1].args.key-resolver=#{@ipKeyResolver}

# EMPLOYEE SERVICE ROUTE
spring.cloud.gateway.routes[1].id=employee-service
spring.cloud.gateway.routes[1].uri=lb://employee-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/employees/**
spring.cloud.gateway.routes[1].filters[0]=StripPrefix=1
spring.cloud.gateway.routes[1].filters[1].name=RequestRateLimiter
spring.cloud.gateway.routes[1].filters[1].args.redis-rate-limiter.replenishRate=5
spring.cloud.gateway.routes[1].filters[1].args.redis-rate-limiter.burstCapacity=15
spring.cloud.gateway.routes[1].filters[1].args.key-resolver=#{@userKeyResolver}

# NOTIFICATION SERVICE ROUTE
spring.cloud.gateway.routes[2].id=notification-service
spring.cloud.gateway.routes[2].uri=lb://notification-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/notifications/**
spring.cloud.gateway.routes[2].filters[0]=StripPrefix=1
spring.cloud.gateway.routes[2].filters[1].name=RequestRateLimiter
spring.cloud.gateway.routes[2].filters[1].args.redis-rate-limiter.replenishRate=5
spring.cloud.gateway.routes[2].filters[1].args.redis-rate-limiter.burstCapacity=15
spring.cloud.gateway.routes[2].filters[1].args.key-resolver=#{@userKeyResolver}

# LOGGING
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.org.springframework.cloud.client.loadbalancer=DEBUG

# AUTH SERVICE CONFIGURATION
# ==========================

server.port=8081
spring.application.name=auth-service
spring.cloud.config.enabled=true

# DATABASE CONFIGURATION - UPDATE CREDENTIALS
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/auth_db
spring.datasource.username=${DB_USERNAME:your_db_username}
spring.datasource.password=${DB_PASSWORD:your_db_password}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA CONFIGURATION
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# FLYWAY MIGRATIONS
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# SUPER ADMIN SEEDING - UPDATE CREDENTIALS
seed.superadmin=true
superadmin.email=${SUPERADMIN_EMAIL:admin@yourcompany.com}
superadmin.password=${SUPERADMIN_PASSWORD:ChangeThisPassword123!}

# SECURITY - UPDATE JWT SECRET
jwt.secret=${JWT_SECRET:your-base64-encoded-jwt-secret-here}
jwt.expiration=${JWT_EXPIRATION:86400000}

# MANAGEMENT & LOGGING
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
logging.level.org.springframework=INFO
logging.level.com.yourcompany=DEBUG

# EMPLOYEE SERVICE CONFIGURATION
# ==============================

server.port=8082
spring.application.name=employee-service
spring.cloud.config.enabled=true

# R2DBC CONFIGURATION - UPDATE CREDENTIALS
spring.r2dbc.url=r2dbc:postgresql://${DB_HOST:localhost}:5432/employee_db
spring.r2dbc.username=${DB_USERNAME:your_db_username}
spring.r2dbc.password=${DB_PASSWORD:your_db_password}

# DATASOURCE CONFIGURATION
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/employee_db
spring.datasource.username=${DB_USERNAME:your_db_username}
spring.datasource.password=${DB_PASSWORD:your_db_password}

# FLYWAY MIGRATIONS
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.enabled=true

# KAFKA PRODUCER
spring.kafka.bootstrap-servers=${KAFKA_BROKER:localhost:9092}
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# SECURITY
jwt.secret=${JWT_SECRET:your-base64-encoded-jwt-secret-here}
jwt.expiration=${JWT_EXPIRATION:86400000}

# MANAGEMENT & LOGGING
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
logging.level.org.springframework=INFO
logging.level.com.yourcompany=DEBUG

# NOTIFICATION SERVICE CONFIGURATION
# ==================================

server.port=8083
spring.application.name=notification-service
spring.cloud.config.enabled=true

# DATABASE CONFIGURATION - UPDATE CREDENTIALS
spring.r2dbc.url=r2dbc:postgresql://${DB_HOST:localhost}:5432/notification_db
spring.r2dbc.username=${DB_USERNAME:your_db_username}
spring.r2dbc.password=${DB_PASSWORD:your_db_password}
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/notification_db
spring.datasource.username=${DB_USERNAME:your_db_username}
spring.datasource.password=${DB_PASSWORD:your_db_password}

# KAFKA CONSUMER
spring.kafka.bootstrap-servers=${KAFKA_BROKER:localhost:9092}
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.yourcompany.shared.event

# FLYWAY MIGRATIONS
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.enabled=true

# SENDGRID CONFIGURATION - UPDATE CREDENTIALS
sendgrid.api-key=${SENDGRID_API_KEY:your-sendgrid-api-key}
sendgrid.from-email=${SENDGRID_FROM_EMAIL:notifications@yourcompany.com}
sendgrid.from-name=${SENDGRID_FROM_NAME:Your Company Name}

# SECURITY
jwt.secret=${JWT_SECRET:your-base64-encoded-jwt-secret-here}
jwt.expiration=${JWT_EXPIRATION:86400000}

# MANAGEMENT & LOGGING
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
logging.level.org.springframework.cloud=DEBUG

🔐 Security Setup Guide
Generate JWT Secret
bash
# Generate a secure base64 encoded JWT secret
openssl rand -base64 64
Update All Placeholders
Replace these placeholders in all configuration files:

Database Credentials
your_db_username → Your PostgreSQL username

your_db_password → Your PostgreSQL password

Security
your-base64-encoded-jwt-secret-here → Generated JWT secret

your-redis-password → Redis server password

Super Admin
admin@yourcompany.com → Actual admin email

ChangeThisPassword123! → Secure admin password

SendGrid
your-sendgrid-api-key → SendGrid API key

notifications@yourcompany.com → Sender email

Your Company Name → Company name

Package Names
com.yourcompany → Your actual package name

com.yourcompany.shared.event → Your shared event package

🚀 Deployment Setup
1. Update Config Server
properties
# In config-server application.properties
spring.cloud.config.server.git.uri=https://github.com/yourusername/employee-management-config.git
spring.cloud.config.server.git.username=your_github_username
spring.cloud.config.server.git.password=your_github_token
2. Set Environment Variables
bash
# Database
export DB_USERNAME=your_production_username
export DB_PASSWORD=your_production_password
export DB_HOST=production-db-host

# Security
export JWT_SECRET=your-generated-jwt-secret
export SUPERADMIN_PASSWORD=secure-admin-password

# Services
export SENDGRID_API_KEY=your-sendgrid-key
export REDIS_PASSWORD=your-redis-password
📋 Pre-Deployment Checklist
All sensitive placeholders replaced with actual values

JWT secret generated and consistent across services

Database credentials updated

SendGrid API key configured

Super admin credentials secured

Package names updated

Environment variables set

Repository set to private

GitHub token configured

🆘 Troubleshooting
Common Issues
Config Server cannot access repository

Verify GitHub token has repo permissions

Check repository is not archived

Services cannot connect to Config Server

Verify Config Server is running on port 8888

Check Eureka registration

JWT validation fails

Ensure same JWT secret across all services

Verify secret is base64 encoded

📞 Support
For configuration issues:

Check service logs for specific error messages

Verify all placeholders have been replaced

Ensure environment variables are set correctly

