# Notification Service - Configuration

Event-driven notification microservice with Kafka and email integration.

## ⚙️ Basic Configuration
# CONFIG SERVER CONNECTION
# MAKE SURE TO DISABLE KRAFKA IF YOU YOU DONT HAVE IT
# SYSTEM WONT WORK IF YOU DONT HAVE KRAFKA

**File**: `src/main/resources/application.properties`

```properties
# CONFIG SERVER CONNECTION
spring.config.import=optional:configserver:http://localhost:8888

# SERVICE CONFIGURATION
spring.application.name=notification-service
server.port=8083

# DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5434/notification_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# EUREKA SERVICE DISCOVERY
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.preferIpAddress=true

# KAFKA CONSUMER CONFIGURATION
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.yourcompany.shared.event

# SENDGRID EMAIL SERVICE
sendgrid.api-key=your_sendgrid_api_key
sendgrid.from-email=your_email@company.com
sendgrid.from-name=YourCompanyName

# FLYWAY MIGRATIONS
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

🔧 Required Setup
Update database credentials with your PostgreSQL username and password

Ensure PostgreSQL is running on port 5434 with database notification_db

Start Eureka Server on port 8761

Start Kafka on port 9092 for event processing

Configure SendGrid API key for email notifications

Update package name in Kafka trusted packages

🚀 Quick Start
bash
mvn spring-boot:run
Service will be available at: http://localhost:8083

📧 Features
Event-driven notifications via Kafka

Email integration with SendGrid

Notification history storage

Asynchronous processing for better performance

For complete documentation on event types, Kafka configuration, and email templates, refer to the comprehensive PDF documentation included with this submission.

text
