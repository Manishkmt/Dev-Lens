package com.devlens.auth.service;

import com.devlens.auth.dto.AuthResponse;
import com.devlens.auth.dto.LoginRequest;
import com.devlens.auth.dto.RegisterRequest;
import com.devlens.common.config.JwtTokenProvider;
import com.devlens.common.config.UserPrincipal;
import com.devlens.common.exception.BadRequestException;
import com.devlens.user.dto.UserProfileDto;
import com.devlens.user.entity.User;
import com.devlens.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user registration, authentication, and JWT issuance
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .defaultLevel("INTERMEDIATE")
                .theme("dark")
                .build();

        User savedUser = userRepository.save(user);
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String token = tokenProvider.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(token)
                .user(UserProfileDto.fromEntity(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userPrincipal);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User record not found"));

        return AuthResponse.builder()
                .token(token)
                .user(UserProfileDto.fromEntity(user))
                .build();
    }
}
