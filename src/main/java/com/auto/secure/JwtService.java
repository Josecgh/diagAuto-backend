package com.auto.secure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  public enum TokenType { ACCESS, REFRESH }

  private final SecretKey signingKey;
  private final long accessMinutes;
  private final long refreshDays;

  public JwtService(
      @Value("${app.jwt.secret}") String base64Secret,
      @Value("${app.jwt.access-minutes:15}") long accessMinutes,
      @Value("${app.jwt.refresh-days:7}") long refreshDays) {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    this.accessMinutes = accessMinutes;
    this.refreshDays = refreshDays;
  }

  public String generateToken(String email, TokenType type) {
    Instant now = Instant.now();
    Instant expiresAt = type == TokenType.ACCESS
        ? now.plus(accessMinutes, ChronoUnit.MINUTES)
        : now.plus(refreshDays, ChronoUnit.DAYS);

    return Jwts.builder()
        .subject(email)
        .claim("token_type", type.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(signingKey)
        .compact();
  }

  public Optional<String> getEmail(String token, TokenType expectedType) {
    if (token == null || token.isBlank()) return Optional.empty();
    try {
      Claims claims = Jwts.parser()
          .verifyWith(signingKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
      if (!expectedType.name().equals(claims.get("token_type", String.class))) return Optional.empty();
      return Optional.ofNullable(claims.getSubject());
    } catch (JwtException | IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
