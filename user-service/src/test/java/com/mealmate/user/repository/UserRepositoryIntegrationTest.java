package com.mealmate.user.repository;

import com.mealmate.user.entity.User;
import com.mealmate.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }


    // =========================================================
    // SAVE
    // =========================================================

    @Test
    void saveUser_shouldSaveUserSuccessfully() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        User savedUser =
                userRepository.save(user);

        assertNotNull(savedUser.getId());

        Optional<User> result =
                userRepository.findById(savedUser.getId());

        assertTrue(result.isPresent());

        User saved = result.get();

        assertEquals("ana", saved.getUsername());
        assertEquals("ana@gmail.com", saved.getEmail());
        assertEquals("Ana", saved.getFirstName());
        assertEquals("Zivkucin", saved.getLastName());
        assertEquals(UserRole.USER, saved.getRole());
        assertTrue(saved.isEnabled());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }


    // =========================================================
    // FIND BY USERNAME
    // =========================================================

    @Test
    void findByUsername_shouldFindUserIgnoringCase() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByUsernameIgnoreCase("ANA");

        assertTrue(result.isPresent());

        assertEquals(
                "ana",
                result.get().getUsername()
        );

        assertEquals(
                "ana@gmail.com",
                result.get().getEmail()
        );
    }


    // =========================================================
    // FIND BY EMAIL
    // =========================================================

    @Test
    void findByEmail_shouldFindUserIgnoringCase() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmailIgnoreCase(
                        "ANA@GMAIL.COM"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "ana",
                result.get().getUsername()
        );

        assertEquals(
                "ana@gmail.com",
                result.get().getEmail()
        );
    }


    // =========================================================
    // EXISTS BY USERNAME
    // =========================================================

    @Test
    void existsByUsername_shouldReturnTrueForExistingUsername() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        userRepository.save(user);

        boolean exists =
                userRepository.existsByUsernameIgnoreCase("ANA");

        assertTrue(exists);
    }


    @Test
    void existsByUsername_shouldReturnFalseForUnknownUsername() {

        boolean exists =
                userRepository.existsByUsernameIgnoreCase(
                        "unknown"
                );

        assertFalse(exists);
    }


    // =========================================================
    // EXISTS BY EMAIL
    // =========================================================

    @Test
    void existsByEmail_shouldReturnTrueForExistingEmail() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        userRepository.save(user);

        boolean exists =
                userRepository.existsByEmailIgnoreCase(
                        "ANA@GMAIL.COM"
                );

        assertTrue(exists);
    }


    @Test
    void existsByEmail_shouldReturnFalseForUnknownEmail() {

        boolean exists =
                userRepository.existsByEmailIgnoreCase(
                        "unknown@gmail.com"
                );

        assertFalse(exists);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        User savedUser =
                userRepository.save(user);

        savedUser.setFirstName("Ana Updated");
        savedUser.setLastName("New Lastname");

        userRepository.save(savedUser);

        Optional<User> result =
                userRepository.findById(savedUser.getId());

        assertTrue(result.isPresent());

        User updatedUser = result.get();

        assertEquals(
                "Ana Updated",
                updatedUser.getFirstName()
        );

        assertEquals(
                "New Lastname",
                updatedUser.getLastName()
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {

        User user = createUser(
                "ana",
                "ana@gmail.com"
        );

        User savedUser =
                userRepository.save(user);

        Long userId = savedUser.getId();

        userRepository.delete(savedUser);

        Optional<User> result =
                userRepository.findById(userId);

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // HELPER
    // =========================================================

    private User createUser(
            String username,
            String email) {

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setFirstName("Ana");
        user.setLastName("Zivkucin");
        user.setRole(UserRole.USER);
        user.setEnabled(true);

        return user;
    }
}