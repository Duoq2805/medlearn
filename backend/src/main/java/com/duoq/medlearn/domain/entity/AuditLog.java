package com.duoq.medlearn.domain.entity;

import com.duoq.medlearn.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)  // NULLABLE for system actions
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AuditAction actionType;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", columnDefinition = "jsonb")
    private Map<String, Object> oldData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", columnDefinition = "jsonb")
    private Map<String, Object> newData;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Factory method for system actions (no user)
    public static AuditLog systemAction(AuditAction action, String entityName, Long entityId) {
        return AuditLog.builder()
                .user(null)
                .actionType(action)
                .entityName(entityName)
                .entityId(entityId)
                .build();
    }

    // Factory method for user actions
    public static AuditLog userAction(User user, AuditAction action, String entityName, Long entityId) {
        return AuditLog.builder()
                .user(user)
                .actionType(action)
                .entityName(entityName)
                .entityId(entityId)
                .build();
    }
}