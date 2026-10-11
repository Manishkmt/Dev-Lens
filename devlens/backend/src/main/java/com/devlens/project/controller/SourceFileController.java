package com.devlens.project.controller;

import com.devlens.common.config.UserPrincipal;
import com.devlens.project.dto.SourceFileDto;
import com.devlens.project.dto.UpdateSourceFileRequest;
import com.devlens.project.service.SourceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for direct file operations by file ID
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class SourceFileController {

    private final SourceFileService sourceFileService;

    @GetMapping("/{id}")
    public ResponseEntity<SourceFileDto> getFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        SourceFileDto file = sourceFileService.getFileById(id, principal.getId());
        return ResponseEntity.ok(file);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceFileDto> updateFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody UpdateSourceFileRequest request) {
        SourceFileDto updated = sourceFileService.updateFile(id, principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        sourceFileService.deleteFile(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
