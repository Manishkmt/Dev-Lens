package com.devlens.project.service;

import com.devlens.common.exception.ResourceNotFoundException;
import com.devlens.project.dto.CreateProjectRequest;
import com.devlens.project.dto.ProjectDto;
import com.devlens.project.dto.SourceFileDto;
import com.devlens.project.dto.UpdateProjectRequest;
import com.devlens.project.entity.Project;
import com.devlens.project.entity.SourceFile;
import com.devlens.project.repository.ProjectRepository;
import com.devlens.project.repository.SourceFileRepository;
import com.devlens.user.entity.User;
import com.devlens.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user projects with strict user ownership scoping
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SourceFileRepository sourceFileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects(Long userId) {
        List<Project> projects = projectRepository.findByOwnerIdOrderByUpdatedAtDesc(userId);
        return projects.stream().map(p -> {
            List<SourceFile> files = sourceFileRepository.findByProjectIdAndOwnerId(p.getId(), userId);
            int fileCount = files.size();
            Integer avgScore = calculateAvgScore(files);
            return ProjectDto.fromEntity(p, fileCount, avgScore, null);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        List<SourceFile> files = sourceFileRepository.findByProjectIdAndOwnerId(project.getId(), userId);
        List<SourceFileDto> fileDtos = files.stream()
                .map(f -> SourceFileDto.fromEntity(f, false))
                .collect(Collectors.toList());

        Integer avgScore = calculateAvgScore(files);
        return ProjectDto.fromEntity(project, files.size(), avgScore, fileDtos);
    }

    @Transactional
    public ProjectDto createProject(Long userId, CreateProjectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Project project = Project.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .language(request.getLanguage().trim().toLowerCase())
                .owner(user)
                .build();

        Project saved = projectRepository.save(project);
        return ProjectDto.fromEntity(saved, 0, 0, List.of());
    }

    @Transactional
    public ProjectDto updateProject(Long id, Long userId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            project.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription().trim());
        }
        if (request.getLanguage() != null && !request.getLanguage().trim().isEmpty()) {
            project.setLanguage(request.getLanguage().trim().toLowerCase());
        }

        Project updated = projectRepository.save(project);
        List<SourceFile> files = sourceFileRepository.findByProjectIdAndOwnerId(updated.getId(), userId);
        Integer avgScore = calculateAvgScore(files);
        return ProjectDto.fromEntity(updated, files.size(), avgScore, null);
    }

    @Transactional
    public void deleteProject(Long id, Long userId) {
        Project project = projectRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        projectRepository.delete(project);
    }

    private Integer calculateAvgScore(List<SourceFile> files) {
        if (files.isEmpty()) return 0;
        double sum = 0;
        int count = 0;
        for (SourceFile f : files) {
            if (f.getQualityScore() != null) {
                sum += f.getQualityScore();
                count++;
            }
        }
        return count > 0 ? (int) Math.round(sum / count) : 85;
    }
}
