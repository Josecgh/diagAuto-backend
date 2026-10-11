package com.auto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auto.auth.dto.CredentialsDTO;
import com.auto.enums.AccountStatus;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private UserRepository userRepository;
  @InjectMocks private AuthService authService;

  @Test
  void authenticatesCredentialsForSessionLogin() {
    Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
        "ana@example.com", null, java.util.List.of());
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authenticated);

    Authentication result = authService.authenticate(new CredentialsDTO("ana@example.com", "password"));

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    assertEquals("ana@example.com", result.getName());
  }

  @Test
  void returnsCurrentUserProfile() {
    UserEntity user = user();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    var result = authService.getMe(user.getEmail());

    assertEquals(user.getEmail(), result.email());
    assertEquals(user.getName(), result.name());
    assertEquals(user.getLastName(), result.lastName());
    assertEquals(user.getAccountStatus(), result.accountStatus());
  }

  @Test
  void returnsNotFoundWhenCurrentUserDoesNotExist() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> authService.getMe("missing@example.com"));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  private UserEntity user() {
    return UserEntity.builder()
        .email("ana@example.com")
        .password("encoded")
        .name("Ana")
        .lastName("Lopez")
        .accountStatus(AccountStatus.FREE)
        .build();
  }
}
