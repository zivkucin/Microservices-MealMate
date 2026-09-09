package com.mealmate.user.service;

import com.mealmate.user.dto.ChangePasswordRequest;
import com.mealmate.user.dto.ChangeRoleRequest;
import com.mealmate.user.dto.ChangeUserStatusRequest;
import com.mealmate.user.dto.UserCreateRequest;
import com.mealmate.user.dto.UserResponse;
import com.mealmate.user.dto.UserUpdateRequest;
import com.mealmate.user.entity.User;
import com.mealmate.user.entity.UserRole;
import com.mealmate.user.exception.InvalidCredentialsException;
import com.mealmate.user.exception.UserAccessDeniedException;
import com.mealmate.user.exception.UserAlreadyExistsException;
import com.mealmate.user.exception.UserNotFoundException;
import com.mealmate.user.mapper.UserMapper;
import com.mealmate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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


    // =========================================================
    // CREATE USER
    // =========================================================

    @Test
    void createUser_shouldCreateUserSuccessfully() {

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("ana");
        request.setEmail("ana@gmail.com");
        request.setPassword("Password123");
        request.setFirstName("Ana");
        request.setLastName("Zivkucin");

        User newUser = new User();

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("ana");
        response.setEmail("ana@gmail.com");
        response.setRole(UserRole.USER);
        response.setEnabled(true);

        when(userRepository.existsByUsernameIgnoreCase("ana"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase("ana@gmail.com"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(newUser);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(newUser))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ana", result.getUsername());
        assertEquals(UserRole.USER, result.getRole());
        assertTrue(result.isEnabled());

        assertEquals("hashedPassword", newUser.getPassword());
        assertEquals(UserRole.USER, newUser.getRole());
        assertTrue(newUser.isEnabled());

        verify(userRepository)
                .save(newUser);

        verify(passwordEncoder)
                .encode("Password123");
    }


    @Test
    void createUser_shouldRejectExistingUsername() {

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("ana");
        request.setEmail("new@gmail.com");
        request.setPassword("Password123");
        request.setFirstName("Ana");
        request.setLastName("Zivkucin");

        when(userRepository.existsByUsernameIgnoreCase("ana"))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, userMapper);
    }


    @Test
    void createUser_shouldRejectExistingEmail() {

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("ana@gmail.com");
        request.setPassword("Password123");
        request.setFirstName("Ana");
        request.setLastName("Zivkucin");

        when(userRepository.existsByUsernameIgnoreCase("newuser"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase("ana@gmail.com"))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, userMapper);
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Test
    void getAllUsers_shouldReturnPagedUsers() {

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setUsername("marko");

        UserResponse firstResponse = new UserResponse();
        firstResponse.setId(1L);
        firstResponse.setUsername("ana");

        UserResponse secondResponse = new UserResponse();
        secondResponse.setId(2L);
        secondResponse.setUsername("marko");

        Page<User> users =
                new PageImpl<>(List.of(user, secondUser));

        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(users);

        when(userMapper.toResponse(user))
                .thenReturn(firstResponse);

        when(userMapper.toResponse(secondUser))
                .thenReturn(secondResponse);

        Page<UserResponse> result =
                userService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals("ana", result.getContent().get(0).getUsername());
        assertEquals("marko", result.getContent().get(1).getUsername());

        verify(userRepository)
                .findAll(any(PageRequest.class));
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Test
    void getUserById_shouldAllowOwner() {

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("ana");

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getUserById(1L, "ana");

        assertEquals(1L, result.getId());
        assertEquals("ana", result.getUsername());
    }


    @Test
    void getUserById_shouldAllowAdminToAccessAnotherUser() {

        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("ana");

        when(userRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getUserById(1L, "admin");

        assertEquals(1L, result.getId());
        assertEquals("ana", result.getUsername());
    }


    @Test
    void getUserById_shouldDenyAnotherRegularUser() {

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("marko");
        otherUser.setRole(UserRole.USER);

        when(userRepository.findByUsernameIgnoreCase("marko"))
                .thenReturn(Optional.of(otherUser));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAccessDeniedException.class,
                () -> userService.getUserById(1L, "marko")
        );

        verify(userMapper, never())
                .toResponse(any());
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Test
    void updateUser_shouldAllowOwnerToUpdateOwnData() {

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("new@gmail.com");
        request.setFirstName("Ana");
        request.setLastName("New");

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("new@gmail.com");
        response.setFirstName("Ana");
        response.setLastName("New");

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmailIgnoreCase("new@gmail.com"))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.updateUser(
                        1L,
                        request,
                        "ana"
                );

        assertEquals("new@gmail.com", user.getEmail());
        assertEquals("New", user.getLastName());
        assertEquals("new@gmail.com", result.getEmail());

        verify(userRepository)
                .save(user);
    }


    @Test
    void updateUser_shouldAllowAdminToUpdateAnotherUser() {

        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(UserRole.ADMIN);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("new@gmail.com");
        request.setFirstName("Ana");
        request.setLastName("New");

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("new@gmail.com");

        when(userRepository.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmailIgnoreCase("new@gmail.com"))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.updateUser(
                        1L,
                        request,
                        "admin"
                );

        assertEquals("new@gmail.com", result.getEmail());
    }


    @Test
    void updateUser_shouldDenyAnotherRegularUser() {

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("marko");
        otherUser.setRole(UserRole.USER);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("new@gmail.com");
        request.setFirstName("Ana");
        request.setLastName("New");

        when(userRepository.findByUsernameIgnoreCase("marko"))
                .thenReturn(Optional.of(otherUser));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAccessDeniedException.class,
                () -> userService.updateUser(
                        1L,
                        request,
                        "marko"
                )
        );

        verify(userRepository, never())
                .save(any());
    }


    @Test
    void updateUser_shouldRejectAlreadyUsedEmail() {

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("existing@gmail.com");
        request.setFirstName("Ana");
        request.setLastName("New");

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmailIgnoreCase("existing@gmail.com"))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(
                        1L,
                        request,
                        "ana"
                )
        );

        verify(userRepository, never())
                .save(any());
    }


    // =========================================================
    // ROLE AND STATUS
    // =========================================================

    @Test
    void changeRole_shouldChangeUserRole() {

        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(UserRole.ADMIN);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setRole(UserRole.ADMIN);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.changeRole(1L, request);

        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(UserRole.ADMIN, result.getRole());

        verify(userRepository)
                .save(user);
    }


    @Test
    void changeStatus_shouldChangeUserStatus() {

        ChangeUserStatusRequest request =
                new ChangeUserStatusRequest();

        request.setEnabled(false);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEnabled(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.changeStatus(1L, request);

        assertFalse(user.isEnabled());
        assertFalse(result.isEnabled());

        verify(userRepository)
                .save(user);
    }


    @Test
    void deleteUser_shouldPerformSoftDelete() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertFalse(user.isEnabled());

        verify(userRepository)
                .save(user);

        verify(userRepository, never())
                .delete(any());
    }


    // =========================================================
    // FIND USER
    // =========================================================

    @Test
    void findByUsername_shouldReturnUser() {

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("ana");

        when(userRepository.findByUsernameIgnoreCase("ana"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.findByUsername("ana");

        assertEquals(1L, result.getId());
        assertEquals("ana", result.getUsername());
    }


    @Test
    void findByEmail_shouldReturnUser() {

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("ana@gmail.com");

        when(userRepository.findByEmailIgnoreCase("ana@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.findByEmail("ana@gmail.com");

        assertEquals(1L, result.getId());
        assertEquals("ana@gmail.com", result.getEmail());
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Test
    void changePassword_shouldChangePasswordSuccessfully() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("Password123");
        request.setNewPassword("NewPassword123");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "hashedPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("NewPassword123"))
                .thenReturn("newHashedPassword");

        userService.changePassword(
                1L,
                request,
                "ana"
        );

        assertEquals(
                "newHashedPassword",
                user.getPassword()
        );

        verify(passwordEncoder)
                .matches("Password123", "hashedPassword");

        verify(passwordEncoder)
                .encode("NewPassword123");

        verify(userRepository)
                .save(user);
    }


    @Test
    void changePassword_shouldDenyAnotherUser() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("Password123");
        request.setNewPassword("NewPassword123");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAccessDeniedException.class,
                () -> userService.changePassword(
                        1L,
                        request,
                        "marko"
                )
        );

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never())
                .save(any());
    }


    @Test
    void changePassword_shouldRejectWrongCurrentPassword() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("WrongPassword");
        request.setNewPassword("NewPassword123");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashedPassword"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.changePassword(
                        1L,
                        request,
                        "ana"
                )
        );

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "hashedPassword"
                );

        verify(passwordEncoder, never())
                .encode("NewPassword123");

        verify(userRepository, never())
                .save(any());
    }
}