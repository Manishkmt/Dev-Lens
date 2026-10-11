package com.devlens.project.dto;

import lombok.*;

/**
 * Request DTO for updating an existing project
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {
    private String name;
    private String description;
    private String language;
}
