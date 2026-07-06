package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.VerificationToken;
import com.duoq.medlearn.domain.enums.UserStatus;
import com.duoq.medlearn.domain.dto.user.UpdateProfileRequest;
import com.duoq.medlearn.domain.dto.user.UserResponse;
import com.duoq.medlearn.exception.EmailAlreadyExistsException;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.exception.UsernameAlreadyExistsException;
import com.duoq.medlearn.mapper.UserMapper;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.repository.VerificationTokenRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.service.EmailService;
import com.duoq.medlearn.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final CurrentUserResolver currentUserResolver;

    @Value("${email.verification.expiry-hours:24}")
    private int expiryHours;

    @Override
    public UserResponse getCurrentUser() {
        User user = resolveCurrentUserEntity();
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = resolveCurrentUserEntity();

        // Username change
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyExistsException();
            }
            log.info("User [{}] changing username: {} -> {}", user.getId(), user.getUsername(), request.getUsername());
            user.setUsername(request.getUsername());
        }

        // Email change 鈥?requires re-verification
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException();
            }

            log.info("User [{}] changing email: {} -> {} 鈥?re-verification required",
                    user.getId(), user.getEmail(), request.getEmail());

            user.setEmail(request.getEmail());
            user.setIsVerified(false);
            user.setStatus(UserStatus.PENDING);

            verificationTokenRepository.findByUserAndUsedAtIsNull(user)
                    .ifPresent(oldToken -> {
                        oldToken.setUsedAt(OffsetDateTime.now());
                        verificationTokenRepository.save(oldToken);
                    });

            String token = generateToken();
            VerificationToken vt = VerificationToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(OffsetDateTime.now().plusHours(expiryHours))
                    .build();
            verificationTokenRepository.save(vt);

            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
        }

        // Profile fields (null = no change)
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    private User resolveCurrentUserEntity() {
        Long userId = currentUserResolver.resolveCurrentUserId();
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
