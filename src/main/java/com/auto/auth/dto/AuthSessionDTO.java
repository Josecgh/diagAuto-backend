package com.auto.auth.dto;

import com.auto.user.dto.UserDTOResponse;

public record AuthSessionDTO(UserDTOResponse user, JwtAuthenticationDTO tokens) {}
