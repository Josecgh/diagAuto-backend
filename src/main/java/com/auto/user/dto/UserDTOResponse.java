package com.auto.user.dto;

import com.auto.enums.AccountStatus;

import lombok.Builder;

@Builder 
public record UserDTOResponse(
  Long id,
  String email,
  String name,
  String lastName,
  AccountStatus accountStatus
) {
  
}
