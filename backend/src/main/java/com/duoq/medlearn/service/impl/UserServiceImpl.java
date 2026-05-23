package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.VerificationToken;
import com.duoq.medlearn.domain.enums.UserStatus;
import com.duoq.medlearn.dto.request.UpdateProfileRequest;
import com.duoq.medlearn.dto.response.UserDTO;
import com.duoq.medlearn.exception.EmailAlreadyExistsException;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.exception.UsernameAlreadyExistsException;
import com.duoq.medlearn.mapper.UserMapper;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.repository.VerificationTokenRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.service.RealtimeChannels;
import com.duoq.medlearn.service.RealtimePublisher;
import com.duoq.medlearn.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailServiceImpl emailService;
    private final RealtimePublisher realtimePublisher;
    private final CurrentUserResolver currentUserResolver;

    @Value("${email.verification.expiry-hours:24}")
    private int expiryHours;

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO getCurrentUser() {
        User user = resolveCurrentUserEntity();
        return userMapper.toUserDTO(user);
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserDTO);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(UpdateProfileRequest request) {
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

        // Email change — requires re-verification
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException();
            }

            log.info("User [{}] changing email: {} -> {} — re-verification required",
                    user.getId(), user.getEmail(), request.getEmail());

            user.setEmail(request.getEmail());
            user.setIsVerified(false);
            user.setStatus(UserStatus.PENDING);

            // Invalidate old unused verification tokens
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
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
        realtimePublisher.publishToUser(
                RealtimeChannels.NOTIFICATION,
                id,
                "notification",
                Map.of(
                        "type", "ACCOUNT_STATUS",
                        "title", "Tài khoản bị vô hiệu hóa",
                        "message", "Tài khoản của bạn đã bị vô hiệu hóa bởi quản trị viên.",
                        "createdAt", OffsetDateTime.now()
                )
        );
        log.info("User [{}] deactivated", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeletedAt(null);
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(UserStatus.PENDING);
        }
        userRepository.save(user);
        realtimePublisher.publishToUser(
                RealtimeChannels.NOTIFICATION,
                id,
                "notification",
                Map.of(
                        "type", "ACCOUNT_STATUS",
                        "title", "Tài khoản được kích hoạt",
                        "message", "Tài khoản của bạn đã được kích hoạt lại.",
                        "createdAt", OffsetDateTime.now()
                )
        );
        log.info("User [{}] activated with status {}", id, user.getStatus());
    }

    @Override
    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.getRoles().add(role);
        userRepository.save(user);
        realtimePublisher.publishToUser(
                RealtimeChannels.NOTIFICATION,
                userId,
                "notification",
                Map.of(
                        "type", "ROLE_UPDATE",
                        "title", "Vai trò được cập nhật",
                        "message", "Bạn đã được gán vai trò: " + roleName,
                        "createdAt", OffsetDateTime.now()
                )
        );
        log.info("Role [{}] assigned to user [{}]", roleName, userId);
    }

    @Override
    @Transactional
    public void removeRole(Long userId, String roleName) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean removed = user.getRoles().removeIf(role -> role.getName().equals(roleName));
        if (!removed) {
            throw new ResourceNotFoundException("User does not have role: " + roleName);
        }
        userRepository.save(user);
        realtimePublisher.publishToUser(
                RealtimeChannels.NOTIFICATION,
                userId,
                "notification",
                Map.of(
                        "type", "ROLE_UPDATE",
                        "title", "Vai trò được cập nhật",
                        "message", "Vai trò đã bị gỡ: " + roleName,
                        "createdAt", OffsetDateTime.now()
                )
        );
        log.info("Role [{}] removed from user [{}]", roleName, userId);
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
