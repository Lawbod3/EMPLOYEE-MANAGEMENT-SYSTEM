# Config Server - Configuration

Centralized configuration server for the employee management system.

## ⚙️ Basic Configuration

**File**: `src/main/resources/application.properties`

```properties
# SERVICE CONFIGURATION
server.port=8888
spring.application.name=config-server

# PRIVATE GITHUB CONFIG REPOSITORY
spring.cloud.config.server.git.uri=https://github.com/your-private-repo-link
spring.cloud.config.server.git.default-label=main
spring.cloud.config.server.git.clone-on-start=true
spring.cloud.config.server.git.force-pull=true
spring.cloud.config.server.git.timeout=30

# GITHUB AUTHENTICATION (PRIVATE REPO)
spring.cloud.config.server.git.username=your_github_username
spring.cloud.config.server.git.password=your_github_token

# CONFIG SERVER ENABLED
spring.cloud.config.server.enabled=true

# EUREKA SERVICE DISCOVERY
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.preferIpAddress=true
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# LOGGING
logging.level.org.springframework.cloud.config=INFO

 Required Setup
Create a PRIVATE GitHub repository for configuration files

Generate GitHub Personal Access Token with repo permissions

Update the Git URI with your private repository URL

Add your GitHub username and token for authentication

Add configuration files (application.yml, service-specific .yml files)

Start Eureka Server before starting config server

🔐 GitHub Token Setup
Go to GitHub Settings → Developer settings → Personal access tokens

Generate new token with repo scope

Use token in the spring.cloud.config.server.git.password property

🚀 Quick Start
bash
mvn spring-boot:run
Config Server will be available at: http://localhost:8888

📝 Configuration Notes
Repository must be PRIVATE for security

GitHub token is required for private repository access

Ensure the configuration repository contains proper YAML/Properties files

Services will connect to this server for centralized configuration

For complete documentation on setting up the private configuration repository and managing environment-specific configurations, refer to the comprehensive PDF documentation included with this submission.

text

