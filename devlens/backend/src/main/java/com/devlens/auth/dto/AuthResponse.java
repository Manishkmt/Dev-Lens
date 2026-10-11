package com.devlens.auth.dto;

import com.devlens.user.dto.UserProfileDto;
import lombok.*;

/**
 * Authentication Response DTO containing JWT token and user profile
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private UserProfileDto user;
}
