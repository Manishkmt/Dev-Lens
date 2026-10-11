package com.devlens.project;

import com.devlens.auth.dto.AuthResponse;
import com.devlens.auth.dto.RegisterRequest;
import com.devlens.project.dto.CreateProjectRequest;
import com.devlens.project.dto.ProjectDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProjectOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken(String name, String email) throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .name(name)
                .email(email)
                .password("Password123!")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        return authResponse.getToken();
    }

    @Test
    @DisplayName("Enforce strict user isolation: User B cannot access or modify User A's project")
    void shouldEnforceProjectOwnershipIsolation() throws Exception {
        String tokenUserA = registerAndGetToken("Alice Student", "alice@devlens.com");
        String tokenUserB = registerAndGetToken("Bob Student", "bob@devlens.com");

        // 1. User A creates a project
        CreateProjectRequest projectReq = CreateProjectRequest.builder()
                .name("Alice Private Compiler")
                .description("LLVM backend experiment")
                .language("java")
                .build();

        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectReq)))
                .andExpect(status().isCreated())
                .andReturn();

        ProjectDto createdProject = objectMapper.readValue(projectResult.getResponse().getContentAsString(), ProjectDto.class);
        Long projectId = createdProject.getId();

        // 2. User A can retrieve their own project
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Private Compiler")));

        // 3. User B CANNOT retrieve User A's project (Returns 404 Not Found to prevent enumeration)
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Project not found")));

        // 4. User B CANNOT delete User A's project
        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isNotFound());

        // 5. User A's project is still intact
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk());
    }
}
