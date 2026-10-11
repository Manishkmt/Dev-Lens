package com.devlens.project.dto;

import com.devlens.project.entity.SourceFile;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO representing a source code file
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceFileDto {
    private Long id;
    private Long projectId;
    private String name;
    private String path;
    private Long size;
    private Integer qualityScore;
    private Integer issuesCount;
    private String content;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAnalyzed;

    public static SourceFileDto fromEntity(SourceFile file, boolean includeContent) {
        long calculatedSize = file.getContent() != null ? file.getContent().length() : 0L;
        return SourceFileDto.builder()
                .id(file.getId())
                .projectId(file.getProject() != null ? file.getProject().getId() : null)
                .name(file.getFileName())
                .path(file.getPath() != null ? file.getPath() : file.getFileName())
                .size(calculatedSize)
                .qualityScore(file.getQualityScore())
                .issuesCount(file.getIssuesCount() != null ? file.getIssuesCount() : 0)
                .content(includeContent ? file.getContent() : null)
                .language(file.getLanguage())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .lastAnalyzed(file.getUpdatedAt() != null ? file.getUpdatedAt() : file.getCreatedAt())
                .build();
    }
}
