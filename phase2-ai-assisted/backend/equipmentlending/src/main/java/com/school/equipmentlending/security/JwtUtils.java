package com.school.equipmentlending.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final String secret;
    private final long expirationMs;
    private SecretKey key;

    public JwtUtils(@Value("${app.jwt.secret:}") String secret,
                    @Value("${app.jwt.expiration-ms:0}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Missing app.jwt.secret in application.properties");
        }
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret too short (use >= 32 chars)");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        if (expirationMs <= 0) {
            throw new IllegalStateException("app.jwt.expiration-ms must be > 0");
        }
    }

    // Generate token from Authentication (includes roles claim)
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            Object claim = claims.get("roles");
            if (claim instanceof List) {
                return ((List<Object>) claim).stream().map(Object::toString).collect(Collectors.toList());
            }
        } catch (JwtException e) {
            // ignore, validation will return false
        }
        return Collections.emptyList();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}
