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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_shouldReturnCreated() throws Exception {

        String requestBody = """
                {
                    "username": "ana",
                    "email": "ana@gmail.com",
                    "password": "Password123",
                    "firstName": "Ana",
                    "lastName": "Zivkucin"
                }
                """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.email").value("ana@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void createUser_withInvalidData_shouldReturnBadRequest()
            throws Exception {

        String requestBody = """
            {
                "username": "",
                "email": "not-an-email",
                "password": "123",
                "firstName": "",
                "lastName": ""
            }
            """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withExistingUsername_shouldReturnConflict()
            throws Exception {

        User existingUser = new User();

        existingUser.setUsername("ana");
        existingUser.setEmail("ana@gmail.com");
        existingUser.setPassword("hashedPassword");
        existingUser.setFirstName("Ana");
        existingUser.setLastName("Zivkucin");
        existingUser.setRole(UserRole.USER);
        existingUser.setEnabled(true);

        userRepository.save(existingUser);

        String requestBody = """
            {
                "username": "ana",
                "email": "other@gmail.com",
                "password": "Password123",
                "firstName": "Ana",
                "lastName": "Other"
            }
            """;

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }@Test
    void getUserById_owner_shouldReturnOk() throws Exception {

        User user = saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER
        );

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        mockMvc.perform(
                        get("/users/" + user.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(user.getId().intValue()))
                .andExpect(jsonPath("$.username")
                        .value("ana"))
                .andExpect(jsonPath("$.email")
                        .value("ana@gmail.com"));
    }



    private User saveUser(
            String username,
            String email,
            UserRole role) {

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setFirstName("Ana");
        user.setLastName("Zivkucin");
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Test
    void getUserById_anotherUser_shouldReturnForbidden()
            throws Exception {

        User ana = saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER
        );

        User marko = saveUser(
                "marko",
                "marko@gmail.com",
                UserRole.USER
        );

        String token = jwtService.generateToken(
                marko.getId(),
                marko.getUsername(),
                marko.getRole().name()
        );

        mockMvc.perform(
                        get("/users/" + ana.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_withoutAuthentication_shouldReturnForbidden()
            throws Exception {

        User user = saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER
        );

        mockMvc.perform(
                        get("/users/" + user.getId())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_admin_shouldReturnOk() throws Exception {

        saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER
        );

        User admin = saveUser(
                "admin",
                "admin@gmail.com",
                UserRole.ADMIN
        );

        String token = jwtService.generateToken(
                admin.getId(),
                admin.getUsername(),
                admin.getRole().name()
        );

        mockMvc.perform(
                        get("/users")
                                .param("page", "0")
                                .param("size", "10")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAllUsers_user_shouldReturnForbidden() throws Exception {

        User user = saveUser(
                "ana",
                "ana@gmail.com",
                UserRole.USER
        );

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        mockMvc.perform(
                        get("/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_owner_shouldReturnOk() throws Exception {
        User user = saveUser("ana", "ana@gmail.com", UserRole.USER);

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        String requestBody = """
            {
                "email": "ana.new@gmail.com",
                "firstName": "Anica",
                "lastName": "Petrovic"
            }
            """;

        mockMvc.perform(
                        put("/users/{id}", user.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.email").value("ana.new@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Anica"))
                .andExpect(jsonPath("$.lastName").value("Petrovic"));
    }

    @Test
    void updateUser_anotherUser_shouldReturnForbidden() throws Exception {
        User ana = saveUser("ana", "ana@gmail.com", UserRole.USER);
        User marko = saveUser("marko", "marko@gmail.com", UserRole.USER);

        String token = jwtService.generateToken(
                marko.getId(),
                marko.getUsername(),
                marko.getRole().name()
        );

        String requestBody = """
            {
                "email": "ana.new@gmail.com",
                "firstName": "AnaNew",
                "lastName": "Petrovic"
            }
            """;

        mockMvc.perform(
                        put("/users/{id}", ana.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void changeRole_admin_shouldReturnOk() throws Exception {
        User admin = saveUser("admin", "admin@gmail.com", UserRole.ADMIN);
        User user = saveUser("ana", "ana@gmail.com", UserRole.USER);

        String token = jwtService.generateToken(
                admin.getId(),
                admin.getUsername(),
                admin.getRole().name()
        );

        String requestBody = """
            {
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch("/users/{id}/role", user.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void changeRole_user_shouldReturnForbidden() throws Exception {
        User user = saveUser("ana", "ana@gmail.com", UserRole.USER);
        User target = saveUser("marko", "marko@gmail.com", UserRole.USER);

        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        String requestBody = """
            {
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch("/users/{id}/role", target.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }
}