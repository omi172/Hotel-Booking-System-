package com.example.HotelBookingSystem.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Ensure this secret is securely configured and long enough for HMAC-SHA256 signatures
    private static final String SECRET_RAW = "SG90ZWxCb29raW5nU3lzdGVtU2VjcmV0S2V5MjAyNlNwcmluZ0Jvb3RNb25nb0RC";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_RAW.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_WINDOW = 1000 * 60 * 60 * 10; // 10 Hours expiration

    public String generateToken(String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_WINDOW))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()             // 1. parserBuilder() replaces parser()
                .setSigningKey(key)  // 2. setSigningKey() replaces verifyWith()
                .build()
                .parseClaimsJws(token)          // 3. parseClaimsJws() replaces parseSignedClaims()
                .getBody();
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String userEmail) {
        final String email = extractEmail(token);
        return (email.equals(userEmail) && !extractClaim(token, Claims::getExpiration).before(new Date()));
    }
}