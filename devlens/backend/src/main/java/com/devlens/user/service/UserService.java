package com.devlens.user.service;

import com.devlens.common.dto.ApiResponse;
import com.devlens.common.exception.BadRequestException;
import com.devlens.common.exception.ResourceNotFoundException;
import com.devlens.user.dto.ChangePasswordRequest;
import com.devlens.user.dto.UpdateProfileRequest;
import com.devlens.user.dto.UserProfileDto;
import com.devlens.user.entity.User;
import com.devlens.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user profile and preferences
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserProfileDto.fromEntity(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getDefaultLevel() != null && !request.getDefaultLevel().trim().isEmpty()) {
            user.setDefaultLevel(request.getDefaultLevel().trim().toUpperCase());
        }
        if (request.getTheme() != null && !request.getTheme().trim().isEmpty()) {
            user.setTheme(request.getTheme().trim().toLowerCase());
        }

        User updatedUser = userRepository.save(user);
        return UserProfileDto.fromEntity(updatedUser);
    }

    @Transactional
    public ApiResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.builder()
                .status(200)
                .message("Password updated successfully")
                .build();
    }
}
