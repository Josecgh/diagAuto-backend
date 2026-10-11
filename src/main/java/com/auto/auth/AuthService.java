package com.auto.auth;

import com.auto.mappers.UserMapper;
import com.auto.secure.JwtService;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import com.auto.user.dto.UserDTOResponse;
import com.auto.auth.dto.AuthSessionDTO;
import com.auto.auth.dto.CredentialsDTO;
import com.auto.auth.dto.JwtAuthenticationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  public AuthSessionDTO login(CredentialsDTO credentials) {
    authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password()));

    UserEntity user = findByEmail(credentials.email());
    return new AuthSessionDTO(UserMapper.toDTO(user), issueTokens(user.getEmail()));
  }

  public JwtAuthenticationDTO refresh(String refreshToken) {
    String email = jwtService.getEmail(refreshToken, JwtService.TokenType.REFRESH)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

    UserEntity user = findByEmail(email);
    return issueTokens(user.getEmail());
  }

  public UserDTOResponse getMe(String email) {
    return UserMapper.toDTO(findByEmail(email));
  }

  private JwtAuthenticationDTO issueTokens(String email) {
    return new JwtAuthenticationDTO(
        jwtService.generateToken(email, JwtService.TokenType.ACCESS),
        jwtService.generateToken(email, JwtService.TokenType.REFRESH));
  }

  private UserEntity findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
