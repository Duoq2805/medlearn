package com.duoq.medlearn.auth.service.impl;

import com.duoq.medlearn.auth.repository.UserSessionRepository;
import com.duoq.medlearn.auth.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionRepository userSessionRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllSessionsImmediately(Long userId) {
        userSessionRepository.revokeAllUserSessions(userId, OffsetDateTime.now());
    }
}
