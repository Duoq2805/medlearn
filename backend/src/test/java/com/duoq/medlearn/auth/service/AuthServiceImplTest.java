package com.duoq.medlearn.auth.service;

import com.duoq.medlearn.auth.entity.*;
import com.duoq.medlearn.auth.service.impl.AuthServiceImpl;
import com.duoq.medlearn.auth.dto.request.*;
import com.duoq.medlearn.auth.dto.response.*;
import com.duoq.medlearn.common.exception.*;
import com.duoq.medlearn.auth.mapper.UserMapper;
import com.duoq.medlearn.auth.repository.*;
import com.duoq.medlearn.common.security.JwtService;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.common.email.EmailService;
import com.duoq.medlearn.auth.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private UserSessionRepository sessionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuditService auditService;
    @Mock private UserSessionService userSessionService;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;
    @InjectMocks private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxAttempts", 5);
        ReflectionTestUtils.setField(authService, "blockDurationMinutes", 15);
        ReflectionTestUtils.setField(authService, "expiryHours", 24);
        ReflectionTestUtils.setField(authService, "passwordResetExpiryHours", 1);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);

        userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setIsVerified(true);
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
    }

    @Test
    void login_shouldSucceed_withValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toAuthResponse(eq(testUser), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(AuthResponse.builder().id(1L).username("testuser").accessToken("token").refreshToken("ref").tokenType("Bearer").expiresIn(3600L).build());

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        verify(sessionRepository).save(any(UserSession.class));
    }

    @Test
    void login_shouldFail_withInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldFail_withUnverifiedEmail() {
        testUser.setIsVerified(false);
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void login_shouldFail_withDeactivatedAccount() {
        testUser.setDeletedAt(OffsetDateTime.now());
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(AccountDeactivatedException.class);
    }

    @Test
    void refreshToken_shouldSucceed_withValidToken() {
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setRefreshToken("valid_refresh_token");
        session.setExpiresAt(OffsetDateTime.now().plusDays(7));
        session.setRevokedAt(null);

        when(sessionRepository.findByRefreshToken("valid_refresh_token")).thenReturn(Optional.of(session));
        when(jwtService.generateToken(any())).thenReturn("new_access_token");
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toAuthResponse(eq(testUser), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(AuthResponse.builder().id(1L).username("testuser").accessToken("token").refreshToken("ref").tokenType("Bearer").expiresIn(3600L).build());

        AuthResponse response = authService.refreshToken("valid_refresh_token");

        assertThat(response).isNotNull();
        verify(sessionRepository, times(2)).save(any(UserSession.class));
    }

    @Test
    void refreshToken_shouldFail_withRevokedToken() {
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setRevokedAt(OffsetDateTime.now().minusHours(1));

        when(sessionRepository.findByRefreshToken("revoked_token")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> authService.refreshToken("revoked_token")).isInstanceOf(TokenReusedException.class);
        verify(userSessionService).revokeAllSessionsImmediately(testUser.getId());
    }

    @Test
    void verifyEmail_shouldSucceed_withValidToken() {
        VerificationToken vt = new VerificationToken();
        vt.setToken("valid_token");
        vt.setUser(testUser);
        vt.setExpiryDate(OffsetDateTime.now().plusHours(1));
        vt.setUsedAt(null);

        when(tokenRepository.findByToken("valid_token")).thenReturn(Optional.of(vt));

        MessageResponse response = authService.verifyEmail("valid_token");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("verified successfully");
        verify(tokenRepository).markAsUsed(eq("valid_token"), any());
        verify(userRepository).save(argThat(u -> Boolean.TRUE.equals(u.getIsVerified())));
    }

    @Test
    void verifyEmail_shouldFail_withExpiredToken() {
        VerificationToken vt = new VerificationToken();
        vt.setToken("expired_token");
        vt.setExpiryDate(OffsetDateTime.now().minusHours(1));

        when(tokenRepository.findByToken("expired_token")).thenReturn(Optional.of(vt));

        assertThatThrownBy(() -> authService.verifyEmail("expired_token"))
                .isInstanceOf(InvalidTokenException.class).hasMessageContaining("expired");
    }

    @Test
    void forgotPassword_shouldSendResetEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MessageResponse response = authService.forgotPassword(request);

        assertThat(response).isNotNull();
        verify(passwordResetTokenRepository).revokeAllUserTokens(testUser.getId());
        verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_shouldSucceed_withValidToken() {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken("reset_token");
        prt.setUser(testUser);
        prt.setExpiryDate(OffsetDateTime.now().plusHours(1));
        prt.setUsedAt(null);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset_token");
        request.setNewPassword("newPassword123");

        when(passwordResetTokenRepository.findByToken("reset_token")).thenReturn(Optional.of(prt));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$encodedNewPassword");

        MessageResponse response = authService.resetPassword(request);

        assertThat(response).isNotNull();
    }

    @Test
    void logout_shouldRevokeSession() {
        UserSession session = new UserSession();
        session.setRefreshToken("refresh_token");

        when(sessionRepository.findByRefreshToken("refresh_token")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.logout("refresh_token", "access_token");

        verify(jwtService).blockToken("access_token");
        verify(sessionRepository).save(argThat(s -> s.getRevokedAt() != null));
    }
}
