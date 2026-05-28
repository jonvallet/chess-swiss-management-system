package com.jonvallet.chess.swiss.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final AppProperties appProperties;
    private final SecretKey signingKey;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAdminToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getExpirationMs()))
                .signWith(signingKey)
                .compact();
    }

    public String generatePlayerToken(UUID playerId, UUID tournamentId) {
        return Jwts.builder()
                .subject(playerId.toString())
                .claim("role", "PLAYER")
                .claim("tournamentId", tournamentId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getExpirationMs()))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public UUID getTournamentId(String token) {
        String tournamentId = parseToken(token).get("tournamentId", String.class);
        return tournamentId != null ? UUID.fromString(tournamentId) : null;
    }

    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }
}
