package com.duoq.medlearn.auth.service.impl;

import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of PermissionService.
 * Loads permissions directly from database (no caching in Phase 1).
 */
@Service("permissionService")
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final CurrentUserResolver currentUserResolver;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(PermissionCode permission) {
        if (permission == null) {
            return false;
        }
        Set<PermissionCode> userPermissions = getCurrentUserPermissions();
        return userPermissions.contains(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String permissionName) {
        if (permissionName == null || permissionName.isBlank()) {
            return false;
        }
        try {
            PermissionCode permission = PermissionCode.valueOf(permissionName);
            return hasPermission(permission);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission name: {}", permissionName);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(PermissionCode... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        Set<PermissionCode> userPermissions = getCurrentUserPermissions();
        return Arrays.stream(permissions)
                .anyMatch(userPermissions::contains);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissionCode> getCurrentUserPermissions() {
        Long userId = currentUserResolver.resolveCurrentUserId();

        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> {
                    try {
                        return PermissionCode.valueOf(permission.getName());
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown permission in database: {}", permission.getName());
                        return null;
                    }
                })
                .filter(permissionCode -> permissionCode != null)
                .collect(Collectors.toSet());
    }
}
