package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndUsedAtIsNull(User user);

    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.usedAt = :now WHERE vt.token = :token")
    int markAsUsed(@Param("token") String token, @Param("now") OffsetDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") OffsetDateTime now);
}