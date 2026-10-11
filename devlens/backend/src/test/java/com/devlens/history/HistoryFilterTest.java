package com.devlens.history;

import com.devlens.analysis.dto.SaveAnalysisRequest;
import com.devlens.auth.dto.AuthResponse;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class HistoryFilterTest {

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
    @DisplayName("Filter history by type and search query, verifying isolation between users")
    void shouldFilterHistoryAndEnforceUserScoping() throws Exception {
        String tokenUserA = registerAndGetToken("History User A", "history.a@devlens.com");
        String tokenUserB = registerAndGetToken("History User B", "history.b@devlens.com");

        // User A saves Analysis 1 (DEBUG)
        SaveAnalysisRequest debugAnalysis = SaveAnalysisRequest.builder()
                .type("DEBUG")
                .language("java")
                .code("int a = 1 / 0;")
                .qualityScore(60)
                .notes("Zero division bug test")
                .build();

        mockMvc.perform(post("/api/analyses")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debugAnalysis)))
                .andExpect(status().isCreated());

        // User A saves Analysis 2 (COMPLEXITY)
        SaveAnalysisRequest complexityAnalysis = SaveAnalysisRequest.builder()
                .type("COMPLEXITY")
                .language("python")
                .code("def binary_search(arr): pass")
                .qualityScore(95)
                .notes("Logarithmic time audit")
                .build();

        mockMvc.perform(post("/api/analyses")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complexityAnalysis)))
                .andExpect(status().isCreated());

        // 1. User A queries all history -> should have 2
        mockMvc.perform(get("/api/history")
                        .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));

        // 2. User A filters by type=DEBUG -> should have 1
        mockMvc.perform(get("/api/history?type=DEBUG")
                        .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].type", is("DEBUG")))
                .andExpect(jsonPath("$.content[0].notes", containsString("Zero division")));

        // 3. User A searches for "Logarithmic" -> should have 1
        mockMvc.perform(get("/api/history?search=Logarithmic")
                        .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].type", is("COMPLEXITY")));

        // 4. User B queries history -> should have 0 (isolation!)
        mockMvc.perform(get("/api/history")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));
    }
}
