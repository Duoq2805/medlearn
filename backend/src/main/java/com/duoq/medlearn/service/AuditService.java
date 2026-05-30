package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;

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
