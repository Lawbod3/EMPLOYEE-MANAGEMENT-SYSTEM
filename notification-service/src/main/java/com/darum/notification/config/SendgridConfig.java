package com.darum.notification.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendgridConfig {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendGridApiKey);
    }

    // These beans are optional since we can use @Value in EmailService
    // But good to have for consistency
    @Bean
    public String fromEmail() {
        return fromEmail;
    }

    @Bean
    public String fromName() {
        return fromName;
    }
}
