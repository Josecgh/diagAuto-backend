package com.auto.auth;

import com.auto.auth.dto.CredentialsDTO;
import com.auto.user.dto.UserDTOResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final SecurityContextRepository securityContextRepository;

  @PostMapping("/login")
  public ResponseEntity<UserDTOResponse> login(
      @Valid @RequestBody CredentialsDTO credentials,
      HttpServletRequest request,
      HttpServletResponse response) {
    Authentication authentication = authService.authenticate(credentials);
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    request.getSession(true);
    request.changeSessionId();
    securityContextRepository.saveContext(context, request, response);
    return ResponseEntity.ok(authService.getMe(authentication.getName()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    if (request.getSession(false) != null) {
      request.getSession(false).invalidate();
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public UserDTOResponse me(Authentication authentication) {
    return authService.getMe(authentication.getName());
  }
}
