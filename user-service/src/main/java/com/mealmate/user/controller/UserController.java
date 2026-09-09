package com.mealmate.user.controller;

import com.mealmate.user.dto.UserCreateRequest;
import com.mealmate.user.dto.UserResponse;
import com.mealmate.user.dto.UserUpdateRequest;
import com.mealmate.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.mealmate.user.dto.ChangeRoleRequest;
import com.mealmate.user.dto.ChangeUserStatusRequest;
import com.mealmate.user.dto.ChangePasswordRequest;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/users")
@Tag(
        name = "Users",
        description = "User management endpoints"
)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody UserCreateRequest request) {

        return userService.createUser(request);
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userService.getAllUsers(pageable);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id,
            Authentication authentication) {

        return userService.getUserById(
                id,
                authentication.getName()
        );
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        return userService.updateUser(
                id,
                request,
                authentication.getName()
        );
    }

    @Operation(summary = "Disable user")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
    }

    @Operation(summary = "Change user role")
    @PatchMapping("/{id}/role")
    public UserResponse changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request) {

        return userService.changeRole(id, request);
    }

    @Operation(summary = "Change user status")
    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserStatusRequest request) {

        return userService.changeStatus(id, request);
    }

    @Operation(summary = "Change user password")
    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        userService.changePassword(
                id,
                request,
                authentication.getName()
        );
    }
}