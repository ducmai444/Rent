package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class JwtUtil {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SECRET_KEY =
            "mj5UiXDGeiUWpdAaVg8aS4UOYa2Pj5Pi";
    private static final long EXPIRED_AT = 30 * 60 * 1000; // 5 min (Millisecond)

    public static String createAccessToken(UserDetails userDetails) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(System.currentTimeMillis() + EXPIRED_AT);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .issuer(userDetails.getUsername())
                .issuedAt(currentDate)
                .expiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    public static boolean isAccessTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public static boolean isValidAccessToken(String token, UserDetails userDetails) {
        if (userDetails.getUsername().equals(extractUsername(token)) && !isAccessTokenExpired(token))
            return true;
        return false;
    }

    public static SecretKey getKey() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return key;
    }

    public static Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public static String extractUsername(String token) {
        return extractAllClaims(token).getIssuer();
    }

    public static String extractRole(String token) {
        return extractAllClaims(token).get("role").toString();
    }

    public static String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);
        String token = null;

        token = authorizationHeader.substring(JwtUtil.BEARER_PREFIX.length());

        return token;
    }
}
