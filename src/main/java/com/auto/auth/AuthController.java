package com.auto.auth;

import com.auto.auth.dto.AuthSessionDTO;
import com.auto.auth.dto.CredentialsDTO;
import com.auto.auth.dto.JwtAuthenticationDTO;
import com.auto.user.dto.UserDTOResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final String REFRESH_PATH = "/api/auth/refresh";
  private final AuthService authService;

  @Value("${app.jwt.access-minutes:15}")
  private long accessTokenMinutes;

  @Value("${app.jwt.refresh-days:7}")
  private long refreshTokenDays;

  @Value("${app.cookie.secure:false}")
  private boolean secureCookies;

  @Value("${app.cookie.same-site:Strict}")
  private String sameSite;

  @GetMapping("/csrf")
  public ResponseEntity<String> csrf(CsrfToken csrfToken) {
    return ResponseEntity.ok(csrfToken.getToken());
  }

  @PostMapping("/login")
  public ResponseEntity<UserDTOResponse> login(@Valid @RequestBody CredentialsDTO credentials) {
    AuthSessionDTO session = authService.login(credentials);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie(session.tokens().token()).toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie(session.tokens().refreshToken()).toString())
        .body(session.user());
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refresh(
      @CookieValue(name = "refresh_token", required = false) String refreshToken) {
    JwtAuthenticationDTO tokens = authService.refresh(refreshToken);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, accessCookie(tokens.token()).toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken()).toString())
        .build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, cookie("access_token", "", "/", Duration.ZERO).toString())
        .header(HttpHeaders.SET_COOKIE, cookie("refresh_token", "", REFRESH_PATH, Duration.ZERO).toString())
        .build();
  }

  @GetMapping("/me")
  public UserDTOResponse me(Authentication authentication) {
    return authService.getMe(authentication.getName());
  }

  private ResponseCookie accessCookie(String value) {
    return cookie("access_token", value, "/", Duration.ofMinutes(accessTokenMinutes));
  }

  private ResponseCookie refreshCookie(String value) {
    return cookie("refresh_token", value, REFRESH_PATH, Duration.ofDays(refreshTokenDays));
  }

  private ResponseCookie cookie(String name, String value, String path, Duration maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secureCookies)
        .sameSite(sameSite)
        .path(path)
        .maxAge(maxAge)
        .build();
  }
}
