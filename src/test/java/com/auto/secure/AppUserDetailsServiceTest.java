package com.auto.secure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auto.enums.AccountStatus;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private AppUserDetailsService userDetailsService;

  @Test
  void loadsEmailPasswordAndUserAuthority() {
    UserEntity user = UserEntity.builder()
        .email("ana@example.com")
        .password("encoded-password")
        .name("Ana")
        .lastName("López")
        .accountStatus(AccountStatus.FREE)
        .build();
    when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));

    UserDetails details = userDetailsService.loadUserByUsername("ana@example.com");

    assertEquals("ana@example.com", details.getUsername());
    assertEquals("encoded-password", details.getPassword());
    assertEquals(1, details.getAuthorities().size());
    assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
    verify(userRepository).findByEmail("ana@example.com");
  }

  @Test
  void throwsWhenEmailDoesNotExist() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class,
        () -> userDetailsService.loadUserByUsername("unknown@example.com"));
  }
}
