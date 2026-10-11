package com.auto.auth;

import com.auto.mappers.UserMapper;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import com.auto.user.dto.UserDTOResponse;
import com.auto.auth.dto.CredentialsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  public Authentication authenticate(CredentialsDTO credentials) {
    return authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password()));
  }

  public UserDTOResponse getMe(String email) {
    return UserMapper.toDTO(findByEmail(email));
  }

  private UserEntity findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
