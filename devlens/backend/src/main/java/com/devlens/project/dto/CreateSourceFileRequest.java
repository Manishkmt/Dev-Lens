package com.devlens.project.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new source file in a project
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSourceFileRequest {

    @NotBlank(message = "File name is required")
    @JsonAlias({"fileName", "name"})
    private String name;

    private String path;

    private String language;

    private String content;

    public String getEffectiveFileName() {
        return name != null ? name.trim() : "";
    }
}
