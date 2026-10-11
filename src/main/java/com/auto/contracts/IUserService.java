package com.auto.contracts;

import com.auto.user.dto.UserDTORequest;
import com.auto.user.dto.UserDTOResponse;

public interface IUserService {

  UserDTOResponse createUser(UserDTORequest request);

  UserDTOResponse getUserById(Long id, String authenticatedEmail);
}
