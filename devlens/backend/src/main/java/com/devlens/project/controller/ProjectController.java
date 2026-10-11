package com.devlens.project.controller;

import com.devlens.common.config.UserPrincipal;
import com.devlens.project.dto.CreateProjectRequest;
import com.devlens.project.dto.CreateSourceFileRequest;
import com.devlens.project.dto.ProjectDto;
import com.devlens.project.dto.SourceFileDto;
import com.devlens.project.dto.UpdateProjectRequest;
import com.devlens.project.service.ProjectService;
import com.devlens.project.service.SourceFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for project management and project-nested files
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SourceFileService sourceFileService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(@AuthenticationPrincipal UserPrincipal principal) {
        List<ProjectDto> projects = projectService.getAllProjects(principal.getId());
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectDto created = projectService.createProject(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id, principal.getId());
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request) {
        ProjectDto updated = projectService.updateProject(id, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        projectService.deleteProject(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<List<SourceFileDto>> getProjectFiles(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        List<SourceFileDto> files = sourceFileService.getFilesByProject(id, principal.getId());
        return ResponseEntity.ok(files);
    }

    @PostMapping("/{id}/files")
    public ResponseEntity<SourceFileDto> addFileToProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CreateSourceFileRequest request) {
        SourceFileDto file = sourceFileService.createFile(id, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(file);
    }
}
