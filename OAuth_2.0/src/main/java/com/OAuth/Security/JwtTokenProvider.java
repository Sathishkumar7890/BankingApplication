package com.OAuth.Security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    
    private static final String SECRET =
            "dummySecretKeyForTesting1234567890!!"; 

    private static final long VALIDITY_MS = 3600000; 

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String clientId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + VALIDITY_MS);

        return Jwts.builder()
                .setSubject(clientId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key) 
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
