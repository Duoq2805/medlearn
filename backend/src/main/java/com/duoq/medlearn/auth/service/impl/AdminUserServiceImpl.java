package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;
import com.duoq.medlearn.domain.enums.UserStatus;
import com.duoq.medlearn.domain.dto.user.UserResponse;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.UserMapper;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.service.AdminUserService;
import com.duoq.medlearn.service.AuditService;
import com.duoq.medlearn.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
        auditService.log(resolveAdmin(), AuditAction.ACCOUNT_DEACTIVATED,
                "User", id, Map.of("targetUserId", id));
        notificationService.publishToUser(
                NotificationService.CHANNEL_NOTIFICATION,
                id,
                "notification",
                Map.of(
                        "type", "ACCOUNT_STATUS",
                        "title", "T脿i kho岷 b峄?v么 hi峄噓 h贸a",
                        "message", "T脿i kho岷 c峄 b岷 膽茫 b峄?v么 hi峄噓 h贸a b峄焛 qu岷 tr峄?vi锚n.",
                        "createdAt", OffsetDateTime.now()
                )
        );
        log.info("User [{}] deactivated", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        // Use findByIdIgnoreDeletedAt to bypass @SQLRestriction for soft-deleted entities
        User user = userRepository.findByIdIgnoreDeletedAt(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeletedAt(null);
        user.setStatus(Boolean.TRUE.equals(user.getIsVerified()) ? UserStatus.ACTIVE : UserStatus.PENDING);
        userRepository.save(user);
        auditService.log(resolveAdmin(), AuditAction.ACCOUNT_ACTIVATED,
                "User", id, Map.of("targetUserId", id, "newStatus", user.getStatus().name()));
        notificationService.publishToUser(
                NotificationService.CHANNEL_NOTIFICATION,
                id,
                "notification",
                Map.of(
                        "type", "ACCOUNT_STATUS",
                        "title", "T脿i kho岷 膽瓢峄 k铆ch ho岷",
                        "message", "T脿i kho岷 c峄 b岷 膽茫 膽瓢峄 k铆ch ho岷 l岷.",
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
        auditService.log(resolveAdmin(), AuditAction.ROLE_ASSIGNED,
                "User", userId,
                Map.of("targetUserId", userId, "roleName", roleName));
        notificationService.publishToUser(
                NotificationService.CHANNEL_NOTIFICATION,
                userId,
                "notification",
                Map.of(
                        "type", "ROLE_UPDATE",
                        "title", "Vai tr貌 膽瓢峄 c岷璸 nh岷璽",
                        "message", "B岷 膽茫 膽瓢峄 g谩n vai tr貌: " + roleName,
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
        auditService.log(resolveAdmin(), AuditAction.ROLE_REMOVED,
                "User", userId,
                Map.of("targetUserId", userId, "roleName", roleName));
        notificationService.publishToUser(
                NotificationService.CHANNEL_NOTIFICATION,
                userId,
                "notification",
                Map.of(
                        "type", "ROLE_UPDATE",
                        "title", "Vai tr貌 膽瓢峄 c岷璸 nh岷璽",
                        "message", "Vai tr貌 膽茫 b峄?g峄? " + roleName,
                        "createdAt", OffsetDateTime.now()
                )
        );
        log.info("Role [{}] removed from user [{}]", roleName, userId);
    }

    private User resolveAdmin() {
        Long adminId = currentUserResolver.resolveCurrentUserId();
        return userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
    }
}
