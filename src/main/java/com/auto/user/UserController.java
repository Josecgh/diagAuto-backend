package com.auto.user;

import com.auto.user.dto.UserDTOResponse;
import com.auto.user.dto.UserDTORequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDTOResponse createUser(@Valid @RequestBody UserDTORequest request) {
    return userService.createUser(request);
  }

  @GetMapping("/{id}")
  public UserDTOResponse getUserById(@PathVariable Long id, Authentication authentication) {
    return userService.getUserById(id, authentication.getName());
  }
}
