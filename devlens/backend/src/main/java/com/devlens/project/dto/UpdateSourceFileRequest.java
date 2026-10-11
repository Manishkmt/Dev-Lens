package com.devlens.project.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

/**
 * Request DTO for updating an existing file's content or name
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSourceFileRequest {

    @JsonAlias({"name", "fileName"})
    private String name;

    private String content;
}
