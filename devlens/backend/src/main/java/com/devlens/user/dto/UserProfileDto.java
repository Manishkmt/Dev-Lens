package com.devlens.user.dto;

import com.devlens.user.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

/**
 * User Profile representation returned in auth and user endpoints
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String defaultLevel;
    private String theme;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileDto fromEntity(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .defaultLevel(user.getDefaultLevel())
                .theme(user.getTheme())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
