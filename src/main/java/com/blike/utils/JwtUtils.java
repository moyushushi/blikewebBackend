package com.blike.utils;

import io.jsonwebtoken.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

public class JwtUtils {
    private static final String SECRET = "your-256-bit-secret-key-for-jwt-signing-please-change";
    private static final SecretKey SECRET_KEY = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
    private static final long EXPIRATION = 7 * 24 * 3600 * 1000L; // 7天

    public static String generateToken(Integer userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}