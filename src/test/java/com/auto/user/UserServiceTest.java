package com.auto.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auto.enums.AccountStatus;
import com.auto.user.dto.UserDTORequest;
import com.auto.user.dto.UserDTOResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UserService userService;

  @Test
  void createUserEncodesPasswordAndSetsFreeAccount() {
    UserDTORequest request = new UserDTORequest("ana@example.com", "password123", "Ana", "López");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserDTOResponse response = userService.createUser(request);

    assertEquals(request.email(), response.email());
    assertEquals("Ana", response.name());
    assertEquals(AccountStatus.FREE, response.accountStatus());
    verify(passwordEncoder).encode("password123");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  void createUserRejectsAnAlreadyRegisteredEmail() {
    UserDTORequest request = new UserDTORequest("ana@example.com", "password123", "Ana", "López");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> userService.createUser(request));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(userRepository, never()).save(any(UserEntity.class));
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void getUserByIdReturnsTheAuthenticatedUsersAccount() {
    UserEntity user = user("ana@example.com");
    when(userRepository.findById(7L)).thenReturn(Optional.of(user));

    UserDTOResponse response = userService.getUserById(7L, "ana@example.com");

    assertEquals("ana@example.com", response.email());
  }

  @Test
  void getUserByIdRejectsAccessToAnotherUsersAccount() {
    when(userRepository.findById(7L)).thenReturn(Optional.of(user("ana@example.com")));

    assertThrows(AccessDeniedException.class,
        () -> userService.getUserById(7L, "otra@example.com"));
  }

  @Test
  void getUserByIdReturnsNotFoundWhenAccountDoesNotExist() {
    when(userRepository.findById(7L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class, () -> userService.getUserById(7L, "ana@example.com"));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  private UserEntity user(String email) {
    return UserEntity.builder()
        .email(email)
        .password("encoded")
        .name("Ana")
        .lastName("López")
        .accountStatus(AccountStatus.FREE)
        .build();
  }
}
