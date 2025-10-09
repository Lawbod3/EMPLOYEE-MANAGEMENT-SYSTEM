package com.darum.auth.service;

import com.darum.shared.security.JwtUtil;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;


@Service
public class JwtTokenService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;


    public String generateToken(String username, List<String> roles) {
        return Jwts.builder().
                subject(username).
                claim("roles", roles).
                issuedAt(new Date()).
                expiration(new Date(System.currentTimeMillis() + jwtExpiration )).
                signWith(getSigningKey()).compact();

    }

    public boolean validateToken(String token) {
        return JwtUtil.validateToken(token, jwtSecret);
    }

    /**
     * Gets username from token - assumes JWT subject contains username/email
     */
    public String getUsernameFromToken(String token) {
        return JwtUtil.extractUsername(token, jwtSecret);
    }

    public List<String> getRolesFromToken(String token) {
        return JwtUtil.extractRoles(token, jwtSecret);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
