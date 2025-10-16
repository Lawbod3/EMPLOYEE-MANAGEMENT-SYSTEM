package com.darum.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGateWayOpenApiConfig {
    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management - API Gateway")
                        .description("Entry point for all microservices (Auth, Employee, Notification, etc.) in the Employee Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Darum Team")
                                .email("lawalsulaimon003@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }


}
