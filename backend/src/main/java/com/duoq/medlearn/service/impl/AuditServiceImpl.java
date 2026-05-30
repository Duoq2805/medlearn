package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.AuditLog;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;
import com.duoq.medlearn.repository.AuditLogRepository;
import com.duoq.medlearn.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Best-effort audit logging.
 *
 * Policy:
 * - Audit failure must NEVER fail the business operation.
 * - Runs in REQUIRES_NEW transaction so a rollback in the
 *   caller does not discard the audit record, and an audit
 *   failure does not rollback the caller.
 * - Never stores passwords, JWTs, refresh tokens, or
 *   verification tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, AuditAction action, String entityName, Long entityId,
                    Map<String, Object> newData) {
        saveEntry(actor, action, entityName, entityId, null, newData, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, AuditAction action, String entityName, Long entityId,
                    Map<String, Object> oldData, Map<String, Object> newData) {
        saveEntry(actor, action, entityName, entityId, oldData, newData, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, AuditAction action, String entityName, Long entityId,
                    Map<String, Object> newData, String reason) {
        saveEntry(actor, action, entityName, entityId, null, newData, reason);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystem(AuditAction action, String entityName, Long entityId,
                          Map<String, Object> newData) {
        saveEntry(null, action, entityName, entityId, null, newData, null);
    }

    private void saveEntry(User actor, AuditAction action, String entityName, Long entityId,
                           Map<String, Object> oldData, Map<String, Object> newData,
                           String reason) {
        try {
            AuditLog entry = AuditLog.builder()
                    .user(actor)
                    .actionType(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .oldData(oldData)
                    .newData(newData)
                    .reason(reason)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Audit log failed: action={}, entity={}/{}", action, entityName, entityId, e);
        }
    }
}
