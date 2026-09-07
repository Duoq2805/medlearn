package com.duoq.medlearn.auth.repository;

import com.duoq.medlearn.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession us WHERE us.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession us WHERE us.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") OffsetDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession us SET us.revokedAt = :now WHERE us.user.id = :userId AND us.revokedAt IS NULL")
    void revokeAllUserSessions(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
}