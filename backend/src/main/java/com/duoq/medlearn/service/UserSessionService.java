package com.duoq.medlearn.service;

/**
 * Handles UserSession lifecycle operations that require
 * independent transaction control.
 */
public interface UserSessionService {

    /**
     * Revoke all active sessions for a user in a separate transaction.
     *
     * Runs in REQUIRES_NEW so the revoke commits immediately,
     * regardless of the caller's transaction outcome.
     *
     * Primary use case: token reuse detection — the caller will
     * throw after this call, which would rollback a shared transaction.
     */
    void revokeAllSessionsImmediately(Long userId);
}
