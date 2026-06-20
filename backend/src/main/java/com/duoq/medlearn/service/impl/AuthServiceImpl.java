package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.security.JwtService;
import com.duoq.medlearn.domain.entity.PasswordResetToken;
import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.UserSession;
import com.duoq.medlearn.domain.entity.VerificationToken;
import com.duoq.medlearn.mapper.UserMapper;
import com.duoq.medlearn.domain.dto.auth.ForgotPasswordRequest;
import com.duoq.medlearn.domain.dto.auth.LoginRequest;
import com.duoq.medlearn.domain.dto.auth.RegisterRequest;
import com.duoq.medlearn.domain.dto.auth.ResendVerificationRequest;
import com.duoq.medlearn.domain.dto.auth.ResetPasswordRequest;
import com.duoq.medlearn.domain.dto.auth.AuthResponse;
import com.duoq.medlearn.domain.dto.auth.MessageResponse;
import com.duoq.medlearn.domain.dto.user.UserDTO;
import com.duoq.medlearn.exception.AccountDeactivatedException;
import com.duoq.medlearn.exception.EmailAlreadyExistsException;
import com.duoq.medlearn.exception.EmailNotVerifiedException;
import com.duoq.medlearn.exception.InvalidCredentialsException;
import com.duoq.medlearn.exception.InvalidTokenException;
import com.duoq.medlearn.exception.RateLimitExceededException;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.exception.TokenReusedException;
import com.duoq.medlearn.exception.UsernameAlreadyExistsException;
import com.duoq.medlearn.repository.PasswordResetTokenRepository;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.repository.UserSessionRepository;
import com.duoq.medlearn.repository.VerificationTokenRepository;
import com.duoq.medlearn.security.CustomUserDetails;
import com.duoq.medlearn.service.AuditService;
import com.duoq.medlearn.service.AuthService;
import com.duoq.medlearn.service.EmailService;
import com.duoq.medlearn.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final UserSessionService userSessionService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Value("${auth.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.rate-limit.block-duration-minutes:15}")
    private int blockDurationMinutes;

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();
    private static class AttemptRecord {
        int count;
        OffsetDateTime blockedUntil;
        AttemptRecord() {
            this.count = 0;
        }
    }


    @Value("${email.verification.expiry-hours:24}")
    private int expiryHours;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    @Value("${jwt.password-reset-expiry-hours:1}")
    private int passwordResetExpiryHours;

    private static final SecureRandom secureRandom = new SecureRandom();

    // ĐĂNG KÝ
    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isVerified(false)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        userRepository.save(user);

        String token = generateToken();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(OffsetDateTime.now().plusHours(expiryHours))
                .build();
        tokenRepository.save(vt);

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
        log.info("User registered: {}", user.getEmail());

        return new MessageResponse("Registration successful! Please check your email to verify your account.");
    }

    // XÁC NHẬN EMAIL
    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken vt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (vt.getUsedAt() != null) {
            throw new InvalidTokenException("Token already used");
        }
        if (vt.getExpiryDate().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        tokenRepository.markAsUsed(token, OffsetDateTime.now());

        User user = vt.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        log.info("Email verified: {}", user.getEmail());
        return new MessageResponse("Email verified successfully! You can now login.");
    }

    // ĐĂNG NHẬP
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String key = request.getUsernameOrEmail().trim().toLowerCase();
        if (isLoginBlocked(key)) {
            throw new RateLimitExceededException("Too many failed login attempts. Please try again later.");
        }

        String invalidHash = "$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6qVQ8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q.";
        User user = userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getUsernameOrEmail()).orElse(null));

        String hashToCheck = user != null ? user.getPasswordHash() : invalidHash;
        boolean passwordValid = passwordEncoder.matches(request.getPassword(), hashToCheck);
        if (user == null || !passwordValid) {
            recordLoginFailure(key);
            throw new InvalidCredentialsException();
        }

        if (!user.getIsVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (user.getDeletedAt() != null) {
            throw new AccountDeactivatedException();
        }

        recordLoginSuccess(key);

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        sessionRepository.revokeAllUserSessions(user.getId(), OffsetDateTime.now());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = generateRefreshToken();

        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(OffsetDateTime.ofInstant(Instant.now().plusMillis(refreshExpiration), ZoneOffset.UTC))
                .build();
        sessionRepository.save(session);

        auditService.log(user, com.duoq.medlearn.domain.enums.AuditAction.LOGIN_SUCCESS,
                "User", user.getId(), Map.of("email", user.getEmail()));

        return userMapper.toAuthResponse(user, accessToken, refreshToken, "Bearer", 86400000L);
    }

    // REFRESH TOKEN
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        User user = session.getUser();

        if (session.getRevokedAt() != null) {
            userSessionService.revokeAllSessionsImmediately(user.getId());
            log.warn("Token reuse detected for user: {}, revoking all sessions", user.getEmail());
            auditService.logSystem(com.duoq.medlearn.domain.enums.AuditAction.TOKEN_REUSE_DETECTED,
                    "UserSession", session.getId(),
                    Map.of("userId", user.getId(), "email", user.getEmail()));
            throw new TokenReusedException();
        }

        if (session.isExpired()) {
            throw new InvalidTokenException("Refresh token expired");
        }

        if (user.getDeletedAt() != null) {
            throw new AccountDeactivatedException();
        }

        session.setRevokedAt(OffsetDateTime.now());
        sessionRepository.save(session);

        // Generate new tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = generateRefreshToken();

        // Create new session
        UserSession newSession = UserSession.builder()
                .user(user)
                .refreshToken(newRefreshToken)
                .expiresAt(OffsetDateTime.ofInstant(Instant.now().plusMillis(refreshExpiration), ZoneOffset.UTC))
                .build();
        sessionRepository.save(newSession);

        return userMapper.toAuthResponse(user, newAccessToken, newRefreshToken, "Bearer", 86400000L);
    }

    // LOGOUT
    @Override
    @Transactional
    public void logout(String refreshToken, String accessToken) {
        if (accessToken != null) {
            jwtService.blockToken(accessToken);
        }
        sessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setRevokedAt(OffsetDateTime.now());
                    sessionRepository.save(session);
                });
    }

    // LẤY USER HIỆN TẠI
    @Override
    public UserDTO getCurrentUser() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidCredentialsException();
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        return userMapper.toUserDTO(user);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "refresh_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // RESEND EMAIL VERIFICATION
    @Override
    @Transactional
    public MessageResponse resendVerification(ResendVerificationRequest request) {
        String successMsg = "If that email is registered and unverified, a new verification link has been sent.";

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || user.getIsVerified()) {
            return new MessageResponse(successMsg);
        }

        // Mark old active token as resent (audit trail)
        tokenRepository.findByUserAndUsedAtIsNull(user)
                .ifPresent(old -> {
                    old.setResentAt(OffsetDateTime.now());
                    tokenRepository.save(old);
                });

        // Create new token
        String newToken = generateToken();
        VerificationToken vt = VerificationToken.builder()
                .token(newToken)
                .user(user)
                .expiryDate(OffsetDateTime.now().plusHours(expiryHours))
                .build();
        tokenRepository.save(vt);

        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), newToken);
        log.info("Verification email resent to: {}", user.getEmail());

        return new MessageResponse(successMsg);
    }

    // FORGOT PASSWORD
    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String successMsg = "If that email is registered, a password reset link has been sent.";

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return new MessageResponse(successMsg);
        }

        // Revoke existing unused tokens
        passwordResetTokenRepository.revokeAllUserTokens(user.getId());

        String token = generateToken();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(OffsetDateTime.now().plusHours(passwordResetExpiryHours))
                .build();
        passwordResetTokenRepository.save(prt);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
        log.info("Password reset email sent to: {}", user.getEmail());

        return new MessageResponse(successMsg);
    }

    // RESET PASSWORD
    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (!prt.isValid()) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.markAsUsed(request.getToken(), OffsetDateTime.now());

        // Revoke all sessions for security
        sessionRepository.revokeAllUserSessions(user.getId(), OffsetDateTime.now());

        auditService.log(user, com.duoq.medlearn.domain.enums.AuditAction.PASSWORD_RESET_COMPLETED,
                "User", user.getId(), Map.of("email", user.getEmail()));

        log.info("Password reset for user: {}", user.getEmail());
        return new MessageResponse("Password has been reset successfully. Please log in with your new password.");
    }

    // --- Login attempt rate-limiting (inlined from LoginAttemptService) ---

    private void recordLoginFailure(String key) {
        AttemptRecord record = attempts.computeIfAbsent(key, k -> new AttemptRecord());
        record.count++;
        if (record.count >= maxAttempts) {
            record.blockedUntil = OffsetDateTime.now().plusMinutes(blockDurationMinutes);
            log.warn("Account/key [{}] blocked until {}", key, record.blockedUntil);
            auditService.logSystem(com.duoq.medlearn.domain.enums.AuditAction.LOGIN_BLOCKED,
                    "User", null,
                    Map.of("attemptKey", key, "attemptCount", record.count,
                           "blockedUntil", record.blockedUntil.toString()));
        }
    }

    private void recordLoginSuccess(String key) {
        attempts.remove(key);
    }

    private boolean isLoginBlocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null || record.blockedUntil == null) {
            return false;
        }
        if (OffsetDateTime.now().isAfter(record.blockedUntil)) {
            attempts.remove(key);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void cleanupExpiredLoginAttemptEntries() {
        OffsetDateTime now = OffsetDateTime.now();
        int removed = 0;
        for (var entry : attempts.entrySet()) {
            AttemptRecord record = entry.getValue();
            if (record.blockedUntil != null && now.isAfter(record.blockedUntil)) {
                attempts.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("AuthService login-attempt cleanup: removed {} expired entries", removed);
        }
    }
}