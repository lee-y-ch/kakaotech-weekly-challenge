package com.community.community.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Integer userId, String email, String nickname) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of(
                        "typ", "access",
                        "email", email,
                        "nickname", nickname
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getAccessTokenExpSeconds())))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public boolean validateAccessToken(String token) {
        try {
            Jws<Claims> claims = parse(token);

            String tokenType = claims.getPayload().get("typ", String.class);

            return "access".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getUserId(String token) {
        Claims claims = parse(token).getPayload();

        return Integer.valueOf(claims.getSubject());
    }

    public long getAccessTokenExpSeconds() {
        return jwtProperties.getAccessTokenExpSeconds();
    }
}
