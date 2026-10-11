package com.devlens.analysis.controller;

import com.devlens.analysis.dto.*;
import com.devlens.analysis.service.CodeAnalysisEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Java static code analysis endpoints
 */
@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
public class AnalyzeController {

    private final CodeAnalysisEngineService analysisEngineService;

    @PostMapping("/errors")
    public ResponseEntity<ErrorAnalysisResponse> analyzeErrors(@Valid @RequestBody AnalyzeRequest request) {
        ErrorAnalysisResponse response = analysisEngineService.analyzeErrors(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complexity")
    public ResponseEntity<ComplexityAnalysisResponse> analyzeComplexity(@Valid @RequestBody AnalyzeRequest request) {
        ComplexityAnalysisResponse response = analysisEngineService.analyzeComplexity(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/review")
    public ResponseEntity<ReviewAnalysisResponse> analyzeReview(@Valid @RequestBody AnalyzeRequest request) {
        ReviewAnalysisResponse response = analysisEngineService.analyzeReview(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/flowchart")
    public ResponseEntity<FlowchartAnalysisResponse> analyzeFlowchart(@Valid @RequestBody AnalyzeRequest request) {
        FlowchartAnalysisResponse response = analysisEngineService.analyzeFlowchart(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/all")
    public ResponseEntity<ComprehensiveAnalysisResponse> analyzeAll(@Valid @RequestBody AnalyzeRequest request) {
        ComprehensiveAnalysisResponse response = analysisEngineService.analyzeAll(request);
        return ResponseEntity.ok(response);
    }
}
