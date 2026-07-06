package com.duoq.medlearn.domain.enums;

/**
 * Enum defining all system permissions.
 * Avoid hardcoded permission strings throughout codebase.
 */
public enum PermissionCode {
    // Disease domain
    DISEASE_WRITE,
    DISEASE_DELETE,
    DISEASE_RESTORE,
    DISEASE_MANAGE,

    // Version domain
    VERSION_READ,
    VERSION_WRITE,
    VERSION_SUBMIT,
    VERSION_REVIEW,
    VERSION_ARCHIVE,
    VERSION_DELETE,
    VERSION_RESTORE,

    // Section domain
    SECTION_WRITE,
    SECTION_EDIT_ANY,

    // User domain
    USER_VIEW_ALL,
    USER_MANAGE,
    ROLE_ASSIGN,

    // System domain
    AUDIT_VIEW,

    // AI domain
    AI_USE,
    AI_SUMMARY,
    AI_MANAGE,
    AI_VIEW_USAGE
}
