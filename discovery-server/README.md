# Discovery Server - Configuration

Eureka Service Discovery server for the employee management system.

## ⚙️ Basic Configuration

**File**: `src/main/resources/application.properties`

```properties
# SERVICE CONFIGURATION
server.port=8761
spring.application.name=discovery-server

# EUREKA SERVER CONFIGURATION
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.enable-self-preservation=false

# LOGGING
logging.level.com.netflix.eureka=INFO
logging.level.com.netflix.discovery=INFO

 Required Setup
No additional dependencies required

Start this service first before all other services

All other services will register with this discovery server

🚀 Quick Start
bash
mvn spring-boot:run
Discovery Server dashboard will be available at: http://localhost:8761

