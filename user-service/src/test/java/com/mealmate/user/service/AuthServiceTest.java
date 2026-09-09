package com.mealmate.user.service;

import com.mealmate.user.dto.LoginRequest;
import com.mealmate.user.dto.LoginResponse;
import com.mealmate.user.entity.User;
import com.mealmate.user.entity.UserRole;
import com.mealmate.user.exception.InvalidCredentialsException;
import com.mealmate.user.exception.UserDisabledException;
import com.mealmate.user.repository.UserRepository;
import com.mealmate.user.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("ana");
        user.setEmail("ana@gmail.com");
        user.setPassword("hashedPassword");
        user.setFirstName("Ana");
        user.setLastName("Zivkucin");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();
        request.setUsername("ana");
        request.setPassword("Password123");

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "hashedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(
                1L,
                "ana",
                "USER"))
                .thenReturn("jwt-token");

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("ana", result.getUsername());
        assertEquals(UserRole.USER, result.getRole());

        verify(userRepository)
                .findByUsernameIgnoreCase("ana");

        verify(passwordEncoder)
                .matches("Password123", "hashedPassword");

        verify(jwtService)
                .generateToken(1L, "ana", "USER");
    }

    @Test
    void shouldRejectUnknownUsername() {

        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("Password123");

        when(userRepository.findByUsernameIgnoreCase("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void shouldRejectWrongPassword() {

        LoginRequest request = new LoginRequest();
        request.setUsername("ana");
        request.setPassword("WrongPassword");

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashedPassword"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder)
                .matches("WrongPassword", "hashedPassword");

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectDisabledUser() {

        LoginRequest request = new LoginRequest();
        request.setUsername("ana");
        request.setPassword("Password123");

        user.setEnabled(false);

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "hashedPassword"))
                .thenReturn(true);

        assertThrows(
                UserDisabledException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder)
                .matches("Password123", "hashedPassword");

        verifyNoInteractions(jwtService);
    }
}