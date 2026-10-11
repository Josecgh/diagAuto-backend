package com.auto.user;

import com.auto.enums.AccountStatus;
import com.auto.contracts.IUserService;
import com.auto.mappers.UserMapper;
import com.auto.user.dto.UserDTORequest;
import com.auto.user.dto.UserDTOResponse;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public UserDTOResponse createUser(UserDTORequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }

    UserEntity user = UserEntity.builder()
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .name(request.name())
        .lastName(request.lastName())
        .accountStatus(AccountStatus.FREE)
        .build();

    return UserMapper.toDTO(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  @Override
  public UserDTOResponse getUserById(Long id, String authenticatedEmail) {
    UserEntity user = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (!user.getEmail().equals(authenticatedEmail)) {
      throw new AccessDeniedException("You can only access your own account");
    }

    return UserMapper.toDTO(user);
  }
}
