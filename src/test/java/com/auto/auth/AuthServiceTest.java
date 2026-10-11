package com.auto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auto.auth.dto.AuthSessionDTO;
import com.auto.auth.dto.CredentialsDTO;
import com.auto.enums.AccountStatus;
import com.auto.secure.JwtService;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private UserRepository userRepository;
  @Mock private JwtService jwtService;
  @InjectMocks private AuthService authService;

  @Test
  void loginAuthenticatesAndReturnsUserAndTokens() {
    UserEntity user = user();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(user.getEmail(), JwtService.TokenType.ACCESS)).thenReturn("access");
    when(jwtService.generateToken(user.getEmail(), JwtService.TokenType.REFRESH)).thenReturn("refresh");

    AuthSessionDTO response = authService.login(new CredentialsDTO(user.getEmail(), "password"));

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    assertEquals(user.getEmail(), response.user().email());
    assertEquals("access", response.tokens().token());
    assertEquals("refresh", response.tokens().refreshToken());
  }

  @Test
  void refreshRejectsInvalidToken() {
    when(jwtService.getEmail("invalid", JwtService.TokenType.REFRESH)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> authService.refresh("invalid"));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  @Test
  void refreshIssuesNewTokensForValidRefreshToken() {
    UserEntity user = user();
    when(jwtService.getEmail("refresh", JwtService.TokenType.REFRESH))
        .thenReturn(Optional.of(user.getEmail()));
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(jwtService.generateToken(user.getEmail(), JwtService.TokenType.ACCESS)).thenReturn("new-access");
    when(jwtService.generateToken(user.getEmail(), JwtService.TokenType.REFRESH)).thenReturn("new-refresh");

    var response = authService.refresh("refresh");

    assertEquals("new-access", response.token());
    assertEquals("new-refresh", response.refreshToken());
  }

  private UserEntity user() {
    return UserEntity.builder()
        .email("ana@example.com")
        .password("encoded")
        .name("Ana")
        .lastName("López")
        .accountStatus(AccountStatus.FREE)
        .build();
  }
}
