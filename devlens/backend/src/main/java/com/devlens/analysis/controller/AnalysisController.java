package com.devlens.analysis.controller;

import com.devlens.analysis.dto.SaveAnalysisRequest;
import com.devlens.analysis.dto.SaveAnalysisResponse;
import com.devlens.analysis.service.AnalysisService;
import com.devlens.common.config.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for saving analysis records
 */
@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<SaveAnalysisResponse> saveAnalysis(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SaveAnalysisRequest request) {
        SaveAnalysisResponse response = analysisService.save(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
