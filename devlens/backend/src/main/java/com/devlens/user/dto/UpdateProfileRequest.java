package com.devlens.user.dto;

import lombok.*;

/**
 * Request DTO for updating user profile and preferences
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String name;
    private String defaultLevel;
    private String theme;
}
