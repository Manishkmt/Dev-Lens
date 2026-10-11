package com.devlens.auth;

import com.devlens.auth.dto.LoginRequest;
import com.devlens.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register successfully registers new user and issues JWT")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test Student")
                .email("student.new@devlens.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("student.new@devlens.com")))
                .andExpect(jsonPath("$.user.name", is("Test Student")))
                .andExpect(jsonPath("$.user.role", is("USER")));
    }

    @Test
    @DisplayName("POST /api/auth/register rejects duplicate email with 400 Bad Request")
    void shouldRejectDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Duplicate User")
                .email("demo@devlens.com") // already seeded or created
                .password("Password123!")
                .build();

        // First registration
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Duplicate registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already registered")));
    }

    @Test
    @DisplayName("POST /api/auth/login authenticates registered user and issues JWT")
    void shouldAuthenticateValidUser() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("Login Test User")
                .email("login.test@devlens.com")
                .password("SecretPassword123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = LoginRequest.builder()
                .email("login.test@devlens.com")
                .password("SecretPassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("login.test@devlens.com")));
    }

    @Test
    @DisplayName("POST /api/auth/login rejects incorrect password with 401 Unauthorized")
    void shouldRejectInvalidCredentials() throws Exception {
        LoginRequest loginReq = LoginRequest.builder()
                .email("demo@devlens.com")
                .password("WrongPassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Invalid email or password")));
    }
}
