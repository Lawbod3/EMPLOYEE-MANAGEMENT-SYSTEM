# Employee Service - Configuration

Employee and Department management microservice with reactive programming.

## ⚙️ Basic Configuration

**File**: `src/main/resources/application.properties`

```properties
# CONFIG SERVER CONNECTION
spring.config.import=optional:configserver:http://localhost:8888

# SERVICE CONFIGURATION
spring.application.name=employee-service
server.port=8082

# DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5433/employee_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.r2dbc.url=r2dbc:postgresql://localhost:5433/employee_db
spring.r2dbc.username=your_db_username
spring.r2dbc.password=your_db_password

# EUREKA SERVICE DISCOVERY
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.preferIpAddress=true

# KAFKA MESSAGE BROKER
spring.kafka.bootstrap-servers=localhost:9092

🔧 Required Setup
Update database credentials with your PostgreSQL username and password

Ensure PostgreSQL is running on port 5433 with database employee_db

Start Eureka Server on port 8761

Start Kafka on port 9092 (optional - for event-driven features)

🚀 Quick Start
bash
mvn spring-boot:run
Service will be available at: http://localhost:8082

📊 Features
Reactive programming for high-performance operations

Employee CRUD operations with role-based access

Department management

Event-driven architecture with Kafka integration