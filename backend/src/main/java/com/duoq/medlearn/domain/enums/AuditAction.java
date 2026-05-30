package com.duoq.medlearn.domain.enums;

public enum AuditAction {

    // Security
    LOGIN_SUCCESS,
    LOGIN_BLOCKED,
    PASSWORD_RESET_COMPLETED,
    TOKEN_REUSE_DETECTED,

    // Admin Account Management
    ACCOUNT_DEACTIVATED,
    ACCOUNT_ACTIVATED,
    ROLE_ASSIGNED,
    ROLE_REMOVED,

    // Moderation
    VERSION_SUBMITTED,
    VERSION_APPROVED,
    VERSION_REJECTED,
    VERSION_ROLLBACK,

    // Business-Critical
    DISEASE_DELETED,
    DISEASE_RESTORED
}
