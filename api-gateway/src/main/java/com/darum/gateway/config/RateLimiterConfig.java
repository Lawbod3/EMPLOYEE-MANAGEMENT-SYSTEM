package com.darum.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {
    // Rate limit by user IP address
    @Bean
    @Primary // This is the go to among all others ..
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ipAddress = Objects.requireNonNull(
                    exchange.getRequest().getRemoteAddress()
                    ).getAddress()
                    .getHostAddress();
            return Mono.just(ipAddress);
        };
    }

        // Rate limit by JWT user ID (for authenticated requests)
        @Bean
        public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }
            // Fallback to IP if no user ID
            String ipAddress = Objects.requireNonNull(
                    exchange.getRequest().getRemoteAddress()
            ).getAddress().getHostAddress();
            return Mono.just(ipAddress);
        };

    }

}
