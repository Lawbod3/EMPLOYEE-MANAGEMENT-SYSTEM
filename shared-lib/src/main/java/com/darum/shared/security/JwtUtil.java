package com.darum.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.List;

public class JwtUtil {

    public static String extractUsername(String token, String secret) {
        // We assume subject = username in our application
        return extractAllClaims(token, secret).getSubject();
    }

    public static List<String> extractRoles(String token, String secret) {
        Claims claims = extractAllClaims(token, secret);
        return claims.get("roles", List.class);
    }

    public static boolean validateToken(String token, String secret) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey(secret))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Claims extractAllClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

}
