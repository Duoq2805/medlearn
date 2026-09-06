package com.duoq.medlearn.security;

import com.duoq.medlearn.exception.InvalidTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import java.util.concurrent.ConcurrentHashMap;
import java.time.OffsetDateTime;

import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final ConcurrentHashMap<String, OffsetDateTime> tokenBlocklist = new ConcurrentHashMap<>();

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public void blockToken(String token) {
        tokenBlocklist.put(token, OffsetDateTime.now().plus(expiration, ChronoUnit.MILLIS));
    }

    public boolean isTokenBlocked(String token) {
        return tokenBlocklist.containsKey(token);
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupBlocklist() {
        OffsetDateTime now = OffsetDateTime.now();
        tokenBlocklist.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateTokenFromEmail(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extracts email with typed exception mapping — use in filter for clear logging.
     */
    public String extractEmailSafely(String token) {
        try {
            return extractEmail(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("JWT token expired");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("JWT token malformed");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("JWT token unsupported");
        } catch (SignatureException e) {
            throw new InvalidTokenException("JWT signature invalid");
        } catch (JwtException e) {
            throw new InvalidTokenException("JWT token invalid");
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}
