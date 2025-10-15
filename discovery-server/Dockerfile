FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar app.jar

# ✅ Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && apt-get clean

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
