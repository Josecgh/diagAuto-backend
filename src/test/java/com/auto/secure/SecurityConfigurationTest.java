package com.auto.secure;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private AuthenticationManager authenticationManager;

  @Test
  void configuresAuthenticationManagerBean() {
    assertNotNull(authenticationManager);
  }

  @Test
  void configuresPasswordEncoderBean() {
    String encoded = passwordEncoder.encode("password123");

    assertNotEquals("password123", encoded);
    assertTrue(passwordEncoder.matches("password123", encoded));
  }

  @Test
  @Test
  void protectedEndpointRejectsAnonymousRequest() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  void vehicleCreationRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/api/vehicles/"))
        .andExpect(status().isForbidden());
  }
}
