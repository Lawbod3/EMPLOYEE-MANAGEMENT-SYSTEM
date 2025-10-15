# API Gateway - Configuration

Central API Gateway for routing and security in the employee management system.

## ⚙️ Basic Configuration

**File**: `src/main/resources/application.properties`

```properties
# CONFIG SERVER CONNECTION
spring.config.import=optional:configserver:http://localhost:8888

# SERVICE CONFIGURATION
spring.application.name=api-gateway
server.port=8080

# REACTIVE WEB APPLICATION TYPE
spring.main.web-application-type=reactive

# EUREKA SERVICE DISCOVERY
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.preferIpAddress=true

# SERVICE DISCOVERY & ROUTING
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true

# LOGGING
logging.level.org.springframework.cloud.gateway=DEBUG