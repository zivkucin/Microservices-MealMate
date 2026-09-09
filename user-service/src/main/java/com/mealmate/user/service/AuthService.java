package com.mealmate.user.service;

import com.mealmate.user.dto.LoginRequest;
import com.mealmate.user.dto.LoginResponse;
import com.mealmate.user.entity.User;
import com.mealmate.user.exception.InvalidCredentialsException;
import com.mealmate.user.exception.UserDisabledException;
import com.mealmate.user.repository.UserRepository;
import com.mealmate.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new UserDisabledException();
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}