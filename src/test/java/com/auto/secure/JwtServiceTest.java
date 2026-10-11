package com.auto.secure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

  @Test
  void generatedAccessTokenContainsEmailForAccessType() {
    JwtService service = new JwtService(SECRET, 15, 7);
    String token = service.generateToken("ana@example.com", JwtService.TokenType.ACCESS);

    assertEquals("ana@example.com",
        service.getEmail(token, JwtService.TokenType.ACCESS).orElseThrow());
  }

  @Test
  void tokenCannotBeUsedAsAnotherTokenType() {
    JwtService service = new JwtService(SECRET, 15, 7);
    String accessToken = service.generateToken("ana@example.com", JwtService.TokenType.ACCESS);

    assertFalse(service.getEmail(accessToken, JwtService.TokenType.REFRESH).isPresent());
  }

  @Test
  void invalidAndMissingTokensReturnEmpty() {
    JwtService service = new JwtService(SECRET, 15, 7);

    assertTrue(service.getEmail(null, JwtService.TokenType.ACCESS).isEmpty());
    assertTrue(service.getEmail("not-a-jwt", JwtService.TokenType.ACCESS).isEmpty());
  }
}
