package com.devlens.analysis.service;

import com.devlens.analysis.dto.SaveAnalysisRequest;
import com.devlens.analysis.dto.SaveAnalysisResponse;
import com.devlens.analysis.entity.Analysis;
import com.devlens.analysis.entity.AnalysisType;
import com.devlens.analysis.repository.AnalysisRepository;
import com.devlens.common.exception.ResourceNotFoundException;
import com.devlens.project.entity.Project;
import com.devlens.project.entity.SourceFile;
import com.devlens.project.repository.ProjectRepository;
import com.devlens.project.repository.SourceFileRepository;
import com.devlens.user.entity.User;
import com.devlens.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing analysis persistence, reusable across modules 2, 3, and 4
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final SourceFileRepository sourceFileRepository;
    private final ObjectMapper objectMapper;

    /**
     * Primary reusable save method callable internally by AI assistance and code engine modules
     */
    @Transactional
    public Analysis save(
            Long userId,
            AnalysisType type,
            String language,
            String codeSnapshot,
            String resultJson,
            Integer qualityScore,
            Long projectId,
            Long fileId,
            String notes) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findByIdAndOwnerId(projectId, userId).orElse(null);
        }

        SourceFile sourceFile = null;
        if (fileId != null) {
            sourceFile = sourceFileRepository.findByIdAndOwnerId(fileId, userId).orElse(null);
            if (sourceFile != null && qualityScore != null) {
                sourceFile.setQualityScore(qualityScore);
                sourceFileRepository.save(sourceFile);
            }
        }

        Analysis analysis = Analysis.builder()
                .user(user)
                .project(project)
                .sourceFile(sourceFile)
                .codeSnapshot(codeSnapshot)
                .language(language != null ? language.toLowerCase() : "java")
                .type(type)
                .resultJson(resultJson)
                .qualityScore(qualityScore)
                .notes(notes)
                .build();

        return analysisRepository.save(analysis);
    }

    /**
     * Public REST save endpoint method
     */
    @Transactional
    public SaveAnalysisResponse save(Long userId, SaveAnalysisRequest request) {
        AnalysisType analysisType;
        try {
            analysisType = AnalysisType.valueOf(request.getType().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            analysisType = AnalysisType.COMPREHENSIVE;
        }

        String resultJsonString = null;
        Integer resolvedScore = request.getQualityScore();

        if (request.getResultJson() != null) {
            JsonNode node = request.getResultJson();
            resultJsonString = node.toString();

            if (resolvedScore == null) {
                if (node.has("qualityScore") && node.get("qualityScore").isInt()) {
                    resolvedScore = node.get("qualityScore").asInt();
                } else if (node.has("overallScore") && node.get("overallScore").isInt()) {
                    resolvedScore = node.get("overallScore").asInt();
                }
            }
        }

        if (resolvedScore == null && (analysisType == AnalysisType.COMPREHENSIVE || analysisType == AnalysisType.REVIEW)) {
            resolvedScore = 88;
        }

        Analysis saved = save(
                userId,
                analysisType,
                request.getLanguage(),
                request.getCode(),
                resultJsonString,
                resolvedScore,
                request.getProjectId(),
                request.getFileId(),
                request.getNotes()
        );

        return SaveAnalysisResponse.builder()
                .id(saved.getId())
                .type(saved.getType().name())
                .language(saved.getLanguage())
                .qualityScore(saved.getQualityScore())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
