package com.auto.secure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;
  @Mock private UserDetailsService userDetailsService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authenticatesRequestWhenAccessCookieContainsValidToken() throws ServletException, IOException {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    MockHttpServletRequest request = requestWithAccessToken("valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    UserDetails user = User.withUsername("ana@example.com")
        .password("encoded")
        .authorities("ROLE_USER")
        .build();
    when(jwtService.getEmail("valid-token", JwtService.TokenType.ACCESS))
        .thenReturn(Optional.of("ana@example.com"));
    when(userDetailsService.loadUserByUsername("ana@example.com")).thenReturn(user);

    filter.doFilter(request, response, chain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication.isAuthenticated());
    assertSame(user, authentication.getPrincipal());
    assertEquals("ana@example.com", authentication.getName());
    assertSame(request, chain.getRequest());
  }

  @Test
  void continuesWithoutAuthenticationWhenCookieIsMissing() throws ServletException, IOException {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertSame(request, chain.getRequest());
    verify(jwtService, never()).getEmail(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  void ignoresRefreshCookieWhenAccessCookieIsMissing() throws ServletException, IOException {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refresh_token", "refresh-value"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).getEmail(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any());
    assertSame(request, chain.getRequest());
  }

  @Test
  void ignoresInvalidAccessToken() throws ServletException, IOException {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    MockHttpServletRequest request = requestWithAccessToken("invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    when(jwtService.getEmail("invalid-token", JwtService.TokenType.ACCESS)).thenReturn(Optional.empty());

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
    assertSame(request, chain.getRequest());
  }

  @Test
  void clearsContextWhenTokenReferencesUnknownUser() throws ServletException, IOException {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    MockHttpServletRequest request = requestWithAccessToken("valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    when(jwtService.getEmail("valid-token", JwtService.TokenType.ACCESS))
        .thenReturn(Optional.of("missing@example.com"));
    when(userDetailsService.loadUserByUsername("missing@example.com"))
        .thenThrow(new UsernameNotFoundException("User not found"));

    filter.doFilter(request, response, chain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertSame(request, chain.getRequest());
  }

  @Test
  void doesNotReplaceExistingAuthentication() throws ServletException, IOException {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    MockHttpServletRequest request = requestWithAccessToken("valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    var existing = UsernamePasswordAuthenticationToken.authenticated("already-authenticated", null, List.of());
    SecurityContextHolder.getContext().setAuthentication(existing);

    filter.doFilter(request, response, chain);

    assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
    verify(jwtService, never()).getEmail(org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any());
    assertSame(request, chain.getRequest());
  }

  private MockHttpServletRequest requestWithAccessToken(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("access_token", token));
    return request;
  }
}
