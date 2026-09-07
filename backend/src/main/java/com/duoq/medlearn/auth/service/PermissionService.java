package com.duoq.medlearn.auth.service;

import com.duoq.medlearn.auth.enums.PermissionCode;

import java.util.Set;

/**
 * Service for checking user permissions.
 * Provides abstraction over permission loading and validation.
 * Used by @PreAuthorize annotations as @permissionService.
 */
public interface PermissionService {

    /**
     * Check if current user has the specified permission.
     */
    boolean hasPermission(PermissionCode permission);

    /**
     * Check if current user has the specified permission by name.
     * Convenience method for @PreAuthorize to avoid fully-qualified enum references in SpEL.
     */
    boolean hasPermission(String permissionName);

    /**
     * Check if current user has any of the specified permissions.
     */
    boolean hasAnyPermission(PermissionCode... permissions);

    /**
     * Get all permissions for the current user.
     */
    Set<PermissionCode> getCurrentUserPermissions();
}
