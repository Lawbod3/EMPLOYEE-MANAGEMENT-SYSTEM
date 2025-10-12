package com.darum.gateway.security;

import com.darum.shared.security.JwtUtil;
import com.darum.shared.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        // Configuration properties if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return this::filter;
    }

    private Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        // Get JWT token from header
        String authHeader = request.getHeaders().getFirst(SecurityConstants.TOKEN_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return unauthorizedResponse(exchange);
        }

        String token = authHeader.substring(7);

        // Validate JWT token using shared library
        if (!JwtUtil.validateToken(token, jwtSecret)) {
            return unauthorizedResponse(exchange);
        }

        // Add user info to headers for downstream services
        try {
            String userId = JwtUtil.extractUserId(token, jwtSecret);
            String username = JwtUtil.extractUsername(token, jwtSecret);
            List<String> roles = JwtUtil.extractRoles(token, jwtSecret);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", username)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return unauthorizedResponse(exchange);
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getPath().toString();
        // Only check paths that come into the GATEWAY
        boolean isPublic = path.startsWith("/api/auth/");
        System.out.println("=== Path: " + path + " | Is Public: " + isPublic);
        return isPublic;
    }
}