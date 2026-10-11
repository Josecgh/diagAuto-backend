package com.auto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auto.auth.dto.CredentialsDTO;
import com.auto.enums.AccountStatus;
import com.auto.user.dto.UserDTOResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;
  @Mock private SecurityContextRepository securityContextRepository;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    authController = new AuthController(authService, securityContextRepository);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void loginAuthenticatesAndSavesSecurityContextInSession() {
    var credentials = new CredentialsDTO("ana@example.com", "password");
    Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
        "ana@example.com", null, java.util.List.of());
    UserDTOResponse user = user();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    when(authService.authenticate(credentials)).thenReturn(authentication);
    when(authService.getMe("ana@example.com")).thenReturn(user);

    var result = authController.login(credentials, request, response);

    assertEquals(user, result.getBody());
    assertEquals(200, result.getStatusCode().value());
    assertEquals("ana@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    assertNotNull(request.getSession(false));

    ArgumentCaptor<SecurityContext> contextCaptor = ArgumentCaptor.forClass(SecurityContext.class);
    verify(securityContextRepository).saveContext(contextCaptor.capture(), same(request), same(response));
    assertEquals("ana@example.com", contextCaptor.getValue().getAuthentication().getName());
  }

  @Test
  void logoutInvalidatesExistingSessionAndClearsAuthentication() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession(session);
    SecurityContextHolder.getContext().setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated("ana@example.com", null, java.util.List.of()));

    var result = authController.logout(request);

    assertEquals(204, result.getStatusCode().value());
    assertTrue(session.isInvalid());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void returnsCurrentUserProfile() {
    UserDTOResponse user = user();
    Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
        "ana@example.com", null, java.util.List.of());
    when(authService.getMe("ana@example.com")).thenReturn(user);

    UserDTOResponse result = authController.me(authentication);

    assertEquals(user, result);
    verify(authService).getMe("ana@example.com");
  }

  private UserDTOResponse user() {
    return UserDTOResponse.builder()
        .id(1L)
        .email("ana@example.com")
        .name("Ana")
        .lastName("Lopez")
        .accountStatus(AccountStatus.FREE)
        .build();
  }
}
