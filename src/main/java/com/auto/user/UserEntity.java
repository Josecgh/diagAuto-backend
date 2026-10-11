package com.auto.user;

import com.auto.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false)
  private AccountStatus accountStatus;

  @Builder
  public UserEntity(String email, String password, String name, String lastName, AccountStatus accountStatus) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.lastName = lastName;
    this.accountStatus = accountStatus;
  }
}
