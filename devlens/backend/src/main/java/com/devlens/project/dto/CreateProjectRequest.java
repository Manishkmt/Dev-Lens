package com.devlens.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new project
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    @NotBlank(message = "Primary language is required")
    private String language;
}
