package com.darum.employee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${auth.service.url:http://auth-service/api/auth/me")
    private String authServiceUrl;

    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(authServiceUrl)
                .build();
    }


}
