package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.AuditLog;
import com.duoq.medlearn.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findAllByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    Page<AuditLog> findAllByActionType(AuditAction actionType, Pageable pageable);
}