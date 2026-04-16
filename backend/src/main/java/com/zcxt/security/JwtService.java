package com.zcxt.security;

import com.zcxt.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String issueToken(String subject, Map<String, Object> claims) {
        var jwt = appProperties.getSecurity().getJwt();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwt.getTtlSeconds());
        return Jwts.builder()
                .issuer(jwt.getIssuer())
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey secretKey() {
        var secret = appProperties.getSecurity().getJwt().getSecret();
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

