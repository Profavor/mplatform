package com.classification.domain_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-sec:1800}")
    private long accessTokenExpirationSec; // default 30 minutes

    @Value("${jwt.refresh-token-expiration-sec:172800}")
    private long refreshTokenExpirationSec; // default 2 days

    private Key key;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret key must be provided via 'jwt.secret' configuration or JWT_SECRET environment variable.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role, String userId) {
        return generateToken(username, role, userId, null);
    }

    public String generateToken(String username, String role, String userId, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("uuid", userId); // backward compatibility
        if (sessionId != null) {
            claims.put("sessionId", sessionId);
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationSec * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
    
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractSessionId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.get("sessionId");
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String generateRefreshToken(String username, String role, String userId) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) claims.put("role", role);
        if (userId != null) claims.put("userId", userId);
        claims.put("tokenType", "REFRESH");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationSec * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
