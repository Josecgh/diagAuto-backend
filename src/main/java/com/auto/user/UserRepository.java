package com.auto.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);
  boolean existsByEmail(String email);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UserEntity u where u.email = :email")
  Optional<UserEntity> findByEmailForUpdate(@Param("email") String email);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from UserEntity u where u.id = :id")
  Optional<UserEntity> findByIdForUpdate(@Param("id") Long id);
}
