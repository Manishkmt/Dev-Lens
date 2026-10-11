package com.devlens.history.service;

import com.devlens.analysis.entity.Analysis;
import com.devlens.analysis.entity.AnalysisType;
import com.devlens.analysis.repository.AnalysisRepository;
import com.devlens.common.exception.ResourceNotFoundException;
import com.devlens.history.dto.HistoryDetailDto;
import com.devlens.history.dto.HistoryItemDto;
import com.devlens.history.dto.HistoryPageResponse;
import com.devlens.history.dto.UpdateHistoryNotesRequest;
import com.devlens.history.dto.UpdateHistoryNotesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user analysis history queries and note updates
 */
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final AnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public HistoryPageResponse getHistory(
            Long userId,
            String search,
            String typeStr,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size) {

        final AnalysisType analysisType;
        if (typeStr != null && !typeStr.trim().isEmpty() && !typeStr.equalsIgnoreCase("ALL")) {
            AnalysisType parsed = null;
            try {
                parsed = AnalysisType.valueOf(typeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
            analysisType = parsed;
        } else {
            analysisType = null;
        }

        final String searchPattern = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;

        Specification<Analysis> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User isolation guarantee
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (analysisType != null) {
                predicates.add(cb.equal(root.get("type"), analysisType));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            if (searchPattern != null) {
                String pattern = "%" + searchPattern + "%";
                Predicate notesPredicate = cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), pattern);
                Predicate langPredicate = cb.like(cb.lower(root.get("language")), pattern);
                Predicate projPredicate = cb.like(cb.lower(root.join("project", JoinType.LEFT).get("name")), pattern);
                predicates.add(cb.or(notesPredicate, langPredicate, projPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Analysis> resultPage = analysisRepository.findAll(spec, pageable);

        List<HistoryItemDto> items = resultPage.getContent().stream().map(a -> {
            String title = buildTitle(a);
            String projectName = a.getProject() != null ? a.getProject().getName() : "Direct Workspace Code";
            int issuesCount = extractIssuesCount(a.getResultJson());

            return HistoryItemDto.builder()
                    .id(a.getId())
                    .title(title)
                    .projectName(projectName)
                    .language(a.getLanguage())
                    .type(a.getType().name())
                    .qualityScore(a.getQualityScore() != null ? a.getQualityScore() : 85)
                    .issuesCount(issuesCount)
                    .notes(a.getNotes())
                    .createdAt(a.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return HistoryPageResponse.builder()
                .content(items)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public HistoryDetailDto getHistoryDetail(Long id, Long userId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("History record not found with id: " + id));

        JsonNode resultJsonNode = null;
        if (analysis.getResultJson() != null && !analysis.getResultJson().isBlank()) {
            try {
                resultJsonNode = objectMapper.readTree(analysis.getResultJson());
            } catch (JsonProcessingException e) {
                resultJsonNode = objectMapper.getNodeFactory().textNode(analysis.getResultJson());
            }
        }

        return HistoryDetailDto.builder()
                .id(analysis.getId())
                .title(buildTitle(analysis))
                .projectId(analysis.getProject() != null ? analysis.getProject().getId() : null)
                .projectName(analysis.getProject() != null ? analysis.getProject().getName() : "Direct Workspace Code")
                .language(analysis.getLanguage())
                .type(analysis.getType().name())
                .code(analysis.getCodeSnapshot())
                .resultJson(resultJsonNode)
                .qualityScore(analysis.getQualityScore())
                .notes(analysis.getNotes())
                .createdAt(analysis.getCreatedAt())
                .build();
    }

    @Transactional
    public UpdateHistoryNotesResponse updateNotes(Long id, Long userId, UpdateHistoryNotesRequest request) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("History record not found with id: " + id));

        analysis.setNotes(request.getNotes() != null ? request.getNotes().trim() : "");
        analysisRepository.save(analysis);

        return UpdateHistoryNotesResponse.builder()
                .id(analysis.getId())
                .notes(analysis.getNotes())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void deleteHistory(Long id, Long userId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("History record not found with id: " + id));

        analysisRepository.delete(analysis);
    }

    private String buildTitle(Analysis analysis) {
        if (analysis.getSourceFile() != null) {
            return analysis.getSourceFile().getFileName() + " " + formatTypeName(analysis.getType());
        }
        String langCap = analysis.getLanguage().substring(0, 1).toUpperCase() + analysis.getLanguage().substring(1);
        return langCap + " Snippet " + formatTypeName(analysis.getType());
    }

    private String formatTypeName(AnalysisType type) {
        switch (type) {
            case EXPLANATION: return "Concept Explanation";
            case DEBUG: return "Debug & Error Diagnostic";
            case COMPLEXITY: return "Complexity Audit";
            case REVIEW: return "Code Quality Review";
            case FLOWCHART: return "Flowchart Diagram";
            case OPTIMIZATION: return "Performance Optimization";
            case DOCS: return "Docstring Generation";
            case COMPARE: return "Code Comparison";
            case COMPREHENSIVE: return "Full Analysis";
            default: return "Analysis";
        }
    }

    private int extractIssuesCount(String resultJson) {
        if (resultJson == null || resultJson.isBlank()) return 0;
        try {
            JsonNode root = objectMapper.readTree(resultJson);
            if (root.has("issuesCount") && root.get("issuesCount").isInt()) {
                return root.get("issuesCount").asInt();
            }
            if (root.has("issues") && root.get("issues").isArray()) {
                return root.get("issues").size();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
