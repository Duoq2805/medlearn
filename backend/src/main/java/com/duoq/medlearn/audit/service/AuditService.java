package com.duoq.medlearn.audit.service;

import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.audit.enums.AuditAction;

import java.util.Map;

public interface AuditService {

    void log(User actor, AuditAction action, String entityName, Long entityId,
             Map<String, Object> newData);

    void log(User actor, AuditAction action, String entityName, Long entityId,
             Map<String, Object> oldData, Map<String, Object> newData);

    void log(User actor, AuditAction action, String entityName, Long entityId,
             Map<String, Object> newData, String reason);

    void logSystem(AuditAction action, String entityName, Long entityId,
                   Map<String, Object> newData);
}
