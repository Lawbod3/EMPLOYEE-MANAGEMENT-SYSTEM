# Auth Service - Configuration

Authentication and Authorization microservice for employee management system.

## ⚙️ Basic Configuration

**File**: `src/main/resources/application.properties`

```properties
# CONFIG SERVER CONNECTION
spring.config.import=optional:configserver:http://localhost:8888

# SERVICE CONFIGURATION
spring.application.name=auth-service
server.port=8081

# DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# EUREKA SERVICE DISCOVERY
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.preferIpAddress=true

# ALTERNATIVE: DISABLE CONFIG SERVER
# spring.cloud.config.enabled=false