package com.mealmate.user.controller;

import com.mealmate.user.entity.User;
import com.mealmate.user.entity.UserRole;
import com.mealmate.user.repository.UserRepository;
import com.mealmate.user.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    private User saveUser(
            String username,
            String email,
            UserRole role,
            boolean enabled
    ) {
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setFirstName("Ana");
        user.setLastName("Zivkucin");
        user.setRole(role);
        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    @Test
    void login_withValidCredentials_shouldReturnOk() throws Exception {
        User user = saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER,
                true
        );

        String requestBody = """
            {
                "username": "ana",
                "password": "Password123"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_withWrongPassword_shouldReturnUnauthorized() throws Exception {
        saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER,
                true
        );

        String requestBody = """
            {
                "username": "ana",
                "password": "WrongPassword"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withUnknownUsername_shouldReturnUnauthorized() throws Exception {
        String requestBody = """
            {
                "username": "unknown",
                "password": "Password123"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withDisabledUser_shouldReturnForbidden() throws Exception {
        saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER,
                false
        );

        String requestBody = """
            {
                "username": "ana",
                "password": "Password123"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }
}