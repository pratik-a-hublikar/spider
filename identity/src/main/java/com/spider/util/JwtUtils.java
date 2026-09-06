package com.spider.util;

import com.spider.enity.core.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey signingKey;
    private final long tokenTtlSeconds;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.login.ttl:86400}") long tokenTtlSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt.plusSeconds(tokenTtlSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
