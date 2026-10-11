package com.auto.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.auto.enums.AccountStatus;
import com.auto.user.UserEntity;
import com.auto.user.dto.UserDTOResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserMapperTest {

  @Test
  void toDTOMapsAllUserResponseFields() {
    UserEntity user = UserEntity.builder()
        .email("ana@example.com")
        .password("hashed-password")
        .name("Ana")
        .lastName("López")
        .accountStatus(AccountStatus.PREMIUM)
        .build();
    ReflectionTestUtils.setField(user, "id", 17L);

    UserDTOResponse response = UserMapper.toDTO(user);

    assertEquals(17L, response.id());
    assertEquals("ana@example.com", response.email());
    assertEquals("Ana", response.name());
    assertEquals("López", response.lastName());
    assertEquals(AccountStatus.PREMIUM, response.accountStatus());
  }

  @Test
  void toDTOReturnsNullWhenEntityIsNull() {
    assertNull(UserMapper.toDTO(null));
  }
}
