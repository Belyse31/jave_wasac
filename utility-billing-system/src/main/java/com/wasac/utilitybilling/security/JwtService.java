package com.wasac.utilitybilling.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTokenMinutes;
    private final long financeAccessTokenMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
                      @Value("${app.jwt.finance-access-token-minutes}") long financeAccessTokenMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        this.financeAccessTokenMinutes = financeAccessTokenMinutes;
    }

    public String generate(UserDetails userDetails) {
        Instant now = Instant.now();
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        long tokenMinutes = roles.contains("ROLE_FINANCE") ? financeAccessTokenMinutes : accessTokenMinutes;
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String subject(String token) {
        return claims(token).getSubject();
    }

    public boolean valid(String token, UserDetails userDetails) {
        return subject(token).equals(userDetails.getUsername()) && claims(token).getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
