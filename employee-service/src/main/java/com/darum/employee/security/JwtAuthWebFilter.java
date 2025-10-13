package com.darum.employee.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthWebFilter implements WebFilter {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange);

        if (token != null) {
            log.debug("Extracted token: {}", token);

            boolean isValid = validateToken(token);

            if (isValid) {
                try {
                    Claims claims = parseToken(token);
                    String username = claims.getSubject();
                    List<String> roles = claims.get("roles", List.class);
                    log.debug("Token validated successfully for user: {} with roles: {}", username, roles);

                    if (username != null) {
                        var authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority(role))
                                .collect(Collectors.toList());

                        var authentication = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    }
                } catch (Exception e) {
                    log.warn("Error processing valid token: {}", e.getMessage());
                }
            } else {
                log.warn("Token validation failed");
            }
        } else {
            log.debug("No JWT token found in request");
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();

            if (claims.getExpiration() != null) {
                boolean isExpired = claims.getExpiration().before(new java.util.Date());
                if (isExpired) {
                    log.warn("Token has expired");
                    return false;
                }
            }

            log.debug("JWT Signature verified successfully");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid signature: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT validation failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected error during token validation: {}", e.getMessage());
        }

        return false;
    }

    private Claims parseToken(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}