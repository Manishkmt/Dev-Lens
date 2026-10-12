package com.devlens.ai;

import com.devlens.ai.dto.*;
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
public class AiControllerTest {

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
    @DisplayName("POST /api/ai/explain returns structured explanation with aiGenerated=true")
    void shouldExplainCode() throws Exception {
        String token = registerAndGetToken("AI Student", "ai.student@devlens.com");

        ExplainDto.Request request = ExplainDto.Request.builder()
                .language("java")
                .code("public class Test { public static void main(String[] args) {} }")
                .level("BEGINNER")
                .build();

        mockMvc.perform(post("/api/ai/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", notNullValue()))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    @Test
    @DisplayName("POST /api/ai/debug returns remediated issues with aiGenerated=true")
    void shouldDebugCode() throws Exception {
        String token = registerAndGetToken("Debug Student", "debug.student@devlens.com");

        DebugDto.Request request = DebugDto.Request.builder()
                .language("java")
                .code("for (int j = 0; j < n - 1; j++) {}")
                .level("INTERMEDIATE")
                .build();

        mockMvc.perform(post("/api/ai/debug")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    @Test
    @DisplayName("POST /api/ai/optimize returns performance refactoring with trade-offs")
    void shouldOptimizeCode() throws Exception {
        String token = registerAndGetToken("Optimize Student", "opt.student@devlens.com");

        OptimizeDto.Request request = OptimizeDto.Request.builder()
                .language("java")
                .code("public static void sort(int[] arr) {}")
                .level("ADVANCED")
                .build();

        mockMvc.perform(post("/api/ai/optimize")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jitStatus", notNullValue()))
                .andExpect(jsonPath("$.tradeOffs", notNullValue()))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    @Test
    @DisplayName("POST /api/ai/docs synthesizes Javadoc and markdown overview")
    void shouldGenerateDocs() throws Exception {
        String token = registerAndGetToken("Docs Student", "docs.student@devlens.com");

        DocsDto.Request request = DocsDto.Request.builder()
                .language("java")
                .code("public static void sort(int[] arr) {}")
                .format("JAVADOC")
                .build();

        mockMvc.perform(post("/api/ai/docs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.javadoc", notNullValue()))
                .andExpect(jsonPath("$.markdown", notNullValue()))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    @Test
    @DisplayName("POST /api/ai/learning provides concept, quiz, and viva defense questions")
    void shouldGenerateLearning() throws Exception {
        String token = registerAndGetToken("Learning Student", "learning.student@devlens.com");

        LearningDto.Request request = LearningDto.Request.builder()
                .topic("Bubble Sort")
                .language("java")
                .level("BEGINNER")
                .build();

        mockMvc.perform(post("/api/ai/learning")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concept.title", notNullValue()))
                .andExpect(jsonPath("$.quiz", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.vivaQuestions", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    @Test
    @DisplayName("POST /api/ai/compare benchmarks two snippets and provides verdict")
    void shouldCompareCode() throws Exception {
        String token = registerAndGetToken("Compare Student", "compare.student@devlens.com");

        CompareDto.Request request = CompareDto.Request.builder()
                .language("java")
                .codeA("public class A { void run() {} }")
                .codeB("public class B { void run() {} }")
                .build();

        mockMvc.perform(post("/api/ai/compare")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics", notNullValue()))
                .andExpect(jsonPath("$.verdict.winner", notNullValue()))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    @Test
    @DisplayName("Assistant Chat: Send message, fetch session history, enforce tenant isolation")
    void shouldChatAndEnforceSessionIsolation() throws Exception {
        String tokenA = registerAndGetToken("Alice Chat", "alice.chat@devlens.com");
        String tokenB = registerAndGetToken("Bob Chat", "bob.chat@devlens.com");

        // 1. User A chats with assistant
        ChatDto.Request chatRequest = ChatDto.Request.builder()
                .message("Explain how loop bounds work in bubble sort")
                .includeContext(true)
                .code("for (int i = 0; i < n; i++)")
                .build();

        MvcResult chatResult = mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.reply", notNullValue()))
                .andExpect(jsonPath("$.aiGenerated", is(true)))
                .andReturn();

        ChatDto.Response chatResponse = objectMapper.readValue(chatResult.getResponse().getContentAsString(), ChatDto.Response.class);
        String sessionId = chatResponse.getSessionId();

        // 2. User A can view sessions list
        mockMvc.perform(get("/api/ai/chat/sessions")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 3. User A can view session detail with message history
        mockMvc.perform(get("/api/ai/chat/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sessionId)))
                .andExpect(jsonPath("$.messages", hasSize(2))); // 1 user + 1 ai message

        // 4. User B CANNOT access User A's chat session (404 Not Found)
        mockMvc.perform(get("/api/ai/chat/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unauthenticated calls to /api/ai/** return 401 Unauthorized")
    void shouldRejectUnauthenticatedRequests() throws Exception {
        ExplainDto.Request request = ExplainDto.Request.builder().code("int x = 1;").build();

        mockMvc.perform(post("/api/ai/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
