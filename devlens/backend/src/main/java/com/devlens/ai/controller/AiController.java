package com.devlens.ai.controller;

import com.devlens.ai.dto.*;
import com.devlens.ai.service.AiFeatureService;
import com.devlens.common.config.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing AI Code Assistance & Pedagogical Learning Endpoints
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiFeatureService aiFeatureService;

    @PostMapping("/explain")
    public ResponseEntity<ExplainDto.Response> explainCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExplainDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.explain(userId, request));
    }

    @PostMapping("/debug")
    public ResponseEntity<DebugDto.Response> debugCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DebugDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.debug(userId, request));
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizeDto.Response> optimizeCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OptimizeDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.optimize(userId, request));
    }

    @PostMapping("/docs")
    public ResponseEntity<DocsDto.Response> generateDocs(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DocsDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.docs(userId, request));
    }

    @PostMapping("/learning")
    public ResponseEntity<LearningDto.Response> generateLearningCurriculum(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody LearningDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.learning(userId, request));
    }

    @PostMapping("/compare")
    public ResponseEntity<CompareDto.Response> compareCode(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompareDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.compare(userId, request));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatDto.Response> chatWithAssistant(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChatDto.Request request) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.chat(userId, request));
    }

    @GetMapping("/chat/sessions")
    public ResponseEntity<List<ChatDto.SessionSummary>> getChatSessions(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.getChatSessions(userId));
    }

    @GetMapping("/chat/sessions/{id}")
    public ResponseEntity<ChatDto.SessionDetail> getChatSessionDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String sessionId) {
        Long userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(aiFeatureService.getChatSessionDetail(userId, sessionId));
    }
}
