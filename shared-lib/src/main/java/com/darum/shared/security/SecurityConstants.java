package com.darum.shared.security;

public class SecurityConstants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final long JWT_EXPIRATION = 86400000; // 24 hours

    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/health"
    };
}
