package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.enums.PermissionCode;

import java.util.Set;

/**
 * Service for checking user permissions.
 * Provides abstraction over permission loading and validation.
 */
public interface PermissionService {

    /**
     * Check if current user has the specified permission.
     */
    boolean hasPermission(PermissionCode permission);

    /**
     * Check if current user has any of the specified permissions.
     */
    boolean hasAnyPermission(PermissionCode... permissions);

    /**
     * Get all permissions for the current user.
     */
    Set<PermissionCode> getCurrentUserPermissions();
}
