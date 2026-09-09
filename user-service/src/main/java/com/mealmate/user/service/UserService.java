package com.mealmate.user.service;

import com.mealmate.user.dto.*;
import com.mealmate.user.entity.User;
import com.mealmate.user.entity.UserRole;
import com.mealmate.user.exception.InvalidCredentialsException;
import com.mealmate.user.exception.UserAlreadyExistsException;
import com.mealmate.user.exception.UserNotFoundException;
import com.mealmate.user.mapper.UserMapper;
import com.mealmate.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mealmate.user.exception.UserAccessDeniedException;



@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    private User getAuthenticatedUser(String username) {

        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with username " + username + " not found"));
    }

    public UserResponse getUserById(
            Long id,
            String authenticatedUsername) {

        User authenticatedUser =
                getAuthenticatedUser(authenticatedUsername);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isOwner = user.getId()
                .equals(authenticatedUser.getId());

        boolean isAdmin = authenticatedUser.getRole()
                .equals(UserRole.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new UserAccessDeniedException();
        }

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(
            Long id,
            UserUpdateRequest request,
            String authenticatedUsername) {

        User authenticatedUser =
                getAuthenticatedUser(authenticatedUsername);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isOwner = user.getId()
                .equals(authenticatedUser.getId());

        boolean isAdmin = authenticatedUser.getRole()
                .equals(UserRole.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new UserAccessDeniedException();
        }

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {

            throw new UserAlreadyExistsException("Email already exists");
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    public UserResponse changeRole(
            Long id,
            ChangeRoleRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(false);

        userRepository.save(user);
    }

    public UserResponse findByUsername(String username) {

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with username " + username + " not found"));

        return userMapper.toResponse(user);
    }

    public UserResponse findByEmail(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"));

        return userMapper.toResponse(user);
    }

    public UserResponse changeStatus(
            Long id,
            ChangeUserStatusRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setEnabled(request.getEnabled());

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    public void changePassword(
            Long id,
            ChangePasswordRequest request,
            String authenticatedUsername) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getUsername().equalsIgnoreCase(authenticatedUsername)) {
            throw new UserAccessDeniedException();
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException();
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }
}