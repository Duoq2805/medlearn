package com.duoq.medlearn.ai.repository;

import com.duoq.medlearn.ai.usage.entity.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {

    @Query("SELECT COALESCE(SUM(u.totalTokens), 0) FROM AiUsageLog u WHERE u.user.id = :userId AND u.createdAt >= :since")
    long sumTokensByUserIdSince(@Param("userId") Long userId, @Param("since") OffsetDateTime since);

    @Query("SELECT COALESCE(SUM(u.totalTokens), 0) FROM AiUsageLog u WHERE u.createdAt >= :since")
    long sumTokensSince(@Param("since") OffsetDateTime since);
}
