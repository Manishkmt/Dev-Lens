package com.devlens.project.dto;

import com.devlens.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing project metadata and optionally contained files
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private String language;
    private Integer qualityScore;
    private Integer fileCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAnalyzed;
    private List<SourceFileDto> files;

    public static ProjectDto fromEntity(Project project, int fileCount, Integer avgQualityScore, List<SourceFileDto> files) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .language(project.getLanguage())
                .qualityScore(avgQualityScore != null ? avgQualityScore : 0)
                .fileCount(fileCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .lastAnalyzed(project.getUpdatedAt() != null ? project.getUpdatedAt() : project.getCreatedAt())
                .files(files)
                .build();
    }
}
