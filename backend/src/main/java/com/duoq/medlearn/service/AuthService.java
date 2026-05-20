package com.duoq.medlearn.service;

import com.duoq.medlearn.config.JwtService;
import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.UserSession;
import com.duoq.medlearn.domain.entity.VerificationToken;
import com.duoq.medlearn.dto.request.LoginRequest;
import com.duoq.medlearn.dto.request.RegisterRequest;
import com.duoq.medlearn.dto.response.AuthResponse;
import com.duoq.medlearn.dto.response.MessageResponse;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.repository.UserSessionRepository;
import com.duoq.medlearn.repository.VerificationTokenRepository;
import com.duoq.medlearn.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${email.verification.expiry-hours:24}")
    private int expiryHours;

    @Value("${jwt.refresh-expiration:604800000}")  // 7 days
    private Long refreshExpiration;

    private static final SecureRandom secureRandom = new SecureRandom();

    // ĐĂNG KÝ
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

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
    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken vt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (vt.getUsedAt() != null) {
            throw new RuntimeException("Token already used");
        }
        if (vt.getExpiryDate().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        tokenRepository.markAsUsed(token, OffsetDateTime.now());

        User user = vt.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        log.info("Email verified: {}", user.getEmail());
        return new MessageResponse("Email verified successfully! You can now login.");
    }

    // ĐĂNG NHẬP
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getUsernameOrEmail())
                        .orElseThrow(() -> new RuntimeException("Invalid credentials")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getIsVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("Account has been deactivated");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        // Revoke old sessions
        sessionRepository.deleteByUserId(user.getId());

        // Generate tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = generateRefreshToken();

        // Save refresh token session - SỬA DÒNG NÀY
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(OffsetDateTime.ofInstant(Instant.now().plusMillis(refreshExpiration), ZoneOffset.UTC))
                .build();
        sessionRepository.save(session);

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .build();
    }

    // REFRESH TOKEN
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!session.isValid()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        User user = session.getUser();

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("Account has been deactivated");
        }

        // ✅ QUAN TRỌNG: Kiểm tra reuse detection
        // Nếu token đã được sử dụng trước đó (revoked) → đánh dấu toàn bộ session của user là compromised
        if (session.getRevokedAt() != null) {
            // Token reuse detected - revoke ALL sessions of this user
            sessionRepository.revokeAllUserSessions(user.getId(), OffsetDateTime.now());
            log.warn("Token reuse detected for user: {}, revoking all sessions", user.getEmail());
            throw new RuntimeException("Security violation: token reuse detected");
        }

        // Revoke old session (ONE TIME USE)
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

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .build();
    }

    // LOGOUT
    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setRevokedAt(OffsetDateTime.now());
                    sessionRepository.save(session);
                });
    }

    // LẤY USER HIỆN TẠI
    public CustomUserDetails getCurrentUser() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        return (CustomUserDetails) auth.getPrincipal();
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
}