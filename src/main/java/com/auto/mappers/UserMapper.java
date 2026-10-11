package com.auto.mappers;

import com.auto.user.UserEntity;
import com.auto.user.dto.UserDTOResponse;

/** Converts user entities into API response DTOs. */
public final class UserMapper {

  private UserMapper() {}

  public static UserDTOResponse toDTO(UserEntity entity) {
    if (entity == null) {
      return null;
    }

    return UserDTOResponse.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .name(entity.getName())
        .lastName(entity.getLastName())
        .accountStatus(entity.getAccountStatus())
        .build();
  }
}
