package com.devlens.user.controller;

import com.devlens.common.config.UserPrincipal;
import com.devlens.common.dto.ApiResponse;
import com.devlens.user.dto.ChangePasswordRequest;
import com.devlens.user.dto.UpdateProfileRequest;
import com.devlens.user.dto.UserProfileDto;
import com.devlens.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authenticated User operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileDto profile = userService.getProfile(principal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request) {
        UserProfileDto updatedProfile = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        ApiResponse response = userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(response);
    }
}
