package com.devlens.project.service;

import com.devlens.common.exception.ResourceNotFoundException;
import com.devlens.project.dto.CreateSourceFileRequest;
import com.devlens.project.dto.SourceFileDto;
import com.devlens.project.dto.UpdateSourceFileRequest;
import com.devlens.project.entity.Project;
import com.devlens.project.entity.SourceFile;
import com.devlens.project.repository.ProjectRepository;
import com.devlens.project.repository.SourceFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing project source files with strict project ownership verification
 */
@Service
@RequiredArgsConstructor
public class SourceFileService {

    private final SourceFileRepository sourceFileRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<SourceFileDto> getFilesByProject(Long projectId, Long userId) {
        // Enforce user ownership of the project
        projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        return sourceFileRepository.findByProjectIdAndOwnerId(projectId, userId).stream()
                .map(file -> SourceFileDto.fromEntity(file, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SourceFileDto getFileById(Long id, Long userId) {
        SourceFile file = sourceFileRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Source file not found with id: " + id));

        return SourceFileDto.fromEntity(file, true);
    }

    @Transactional
    public SourceFileDto createFile(Long projectId, Long userId, CreateSourceFileRequest request) {
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        String fileName = request.getEffectiveFileName();
        String language = request.getLanguage();
        if (language == null || language.isBlank()) {
            language = detectLanguage(fileName, project.getLanguage());
        }

        SourceFile file = SourceFile.builder()
                .fileName(fileName)
                .path(request.getPath() != null && !request.getPath().isBlank() ? request.getPath() : fileName)
                .language(language.toLowerCase())
                .content(request.getContent() != null ? request.getContent() : "")
                .qualityScore(null)
                .issuesCount(0)
                .project(project)
                .build();

        SourceFile saved = sourceFileRepository.save(file);
        return SourceFileDto.fromEntity(saved, true);
    }

    @Transactional
    public SourceFileDto updateFile(Long id, Long userId, UpdateSourceFileRequest request) {
        SourceFile file = sourceFileRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Source file not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            file.setFileName(request.getName().trim());
        }
        if (request.getContent() != null) {
            file.setContent(request.getContent());
        }

        SourceFile updated = sourceFileRepository.save(file);
        return SourceFileDto.fromEntity(updated, true);
    }

    @Transactional
    public void deleteFile(Long id, Long userId) {
        SourceFile file = sourceFileRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Source file not found with id: " + id));

        sourceFileRepository.delete(file);
    }

    private String detectLanguage(String fileName, String defaultLang) {
        if (fileName == null) return defaultLang;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".h")) return "cpp";
        if (lower.endsWith(".c")) return "c";
        if (lower.endsWith(".js")) return "javascript";
        if (lower.endsWith(".ts")) return "typescript";
        if (lower.endsWith(".html")) return "html";
        if (lower.endsWith(".css")) return "css";
        return defaultLang != null ? defaultLang : "java";
    }
}
