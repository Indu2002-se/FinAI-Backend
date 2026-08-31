package com.finai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finai.backend.dto.request.LoginRequest;
import com.finai.backend.dto.request.RegisterRequest;
import com.finai.backend.entity.Role;
import com.finai.backend.entity.User;
import com.finai.backend.entity.enums.RoleType;
import com.finai.backend.repository.RoleRepository;
import com.finai.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testEmail = "auth-test-" + suffix + "@example.com";
        testPassword = "ValidPassword123!";

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_USER).build()));

        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(testEmail)
                .password(passwordEncoder.encode(testPassword))
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(true)
                .profileComplete(true)
                .build();
        user.addRole(userRole);
        userRepository.save(user);
    }

    /**
     * TC001 – Valid Authentication
     * Verify successful authentication with valid credentials.
     */
    @Test
    @DisplayName("TC001: loginWithValidCredentialsShouldAuthenticateUser")
    void loginWithValidCredentialsShouldAuthenticateUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Login successful")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.token", not(emptyString())))
                .andExpect(jsonPath("$.data.type", is("Bearer")))
                .andExpect(jsonPath("$.data.user.email", is(testEmail)))
                .andExpect(jsonPath("$.data.userType", is("PARENT")));
    }

    /**
     * TC002 – Invalid Authentication
     * Verify authentication failure with invalid credentials.
     */
    @Test
    @DisplayName("TC002: loginWithInvalidCredentialsShouldRejectAuthentication")
    void loginWithInvalidCredentialsShouldRejectAuthentication() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(testEmail)
                .password("WrongPassword999!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("Register with valid details should create user and return token")
    void registerWithValidDetailsShouldCreateUser() throws Exception {
        String newEmail = "register-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email(newEmail)
                .password("RegisterPass123!")
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is(newEmail)));
    }
}
