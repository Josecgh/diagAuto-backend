package com.auto.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CredentialsDTO(
  @NotBlank
  String email,

  @NotBlank
  String password
) {
  
}
