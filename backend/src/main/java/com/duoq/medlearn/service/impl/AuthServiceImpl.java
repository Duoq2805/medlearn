package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.config.JwtService;
import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.UserSession;
import com.duoq.medlearn.domain.entity.VerificationToken;
import com.duoq.medlearn.mapper.UserMapper;
import com.duoq.medlearn.dto.request.LoginRequest;
import com.duoq.medlearn.dto.request.RegisterRequest;
import com.duoq.medlearn.dto.response.AuthResponse;
import com.duoq.medlearn.dto.response.MessageResponse;
import com.duoq.medlearn.dto.response.UserDTO;
import com.duoq.medlearn.exception.AccountDeactivatedException;
import com.duoq.medlearn.exception.EmailAlreadyExistsException;
import com.duoq.medlearn.exception.EmailNotVerifiedException;
import com.duoq.medlearn.exception.InvalidCredentialsException;
import com.duoq.medlearn.exception.InvalidTokenException;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.exception.TokenReusedException;
import com.duoq.medlearn.exception.UsernameAlreadyExistsException;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.repository.UserSessionRepository;
import com.duoq.medlearn.repository.VerificationTokenRepository;
import com.duoq.medlearn.security.CustomUserDetails;
import com.duoq.medlearn.service.AuthService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository tokenRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailServiceImpl emailService;
    private final UserMapper userMapper;

    @Value("${email.verification.expiry-hours:24}")
    private int expiryHours;

    @Value("${jwt.refresh-expiration:604800000}")  // 7 days
    private Long refreshExpiration;

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
        String invalidHash = "$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6qVQ8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q8Q.";
        User user = userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getUsernameOrEmail()).orElse(null));

        String hashToCheck = user != null ? user.getPasswordHash() : invalidHash;
        boolean passwordValid = passwordEncoder.matches(request.getPassword(), hashToCheck);
        if (user == null || !passwordValid) {
            throw new InvalidCredentialsException();
        }

        if (!user.getIsVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (user.getDeletedAt() != null) {
            throw new AccountDeactivatedException();
        }

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        // Revoke old sessions
        sessionRepository.revokeAllUserSessions(user.getId(), OffsetDateTime.now());

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
            sessionRepository.revokeAllUserSessions(user.getId(), OffsetDateTime.now());
            log.warn("Token reuse detected for user: {}, revoking all sessions", user.getEmail());
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
    public void logout(String refreshToken) {
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
}