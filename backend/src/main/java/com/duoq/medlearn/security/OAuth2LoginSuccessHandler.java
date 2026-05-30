package com.duoq.medlearn.security;

import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.entity.UserSession;
import com.duoq.medlearn.repository.UserSessionRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JwtService jwtService;
    private final UserSessionRepository sessionRepository;

    @Value("${app.url}")
    private String appUrl;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        sessionRepository.revokeAllUserSessions(user.getId(), OffsetDateTime.now());

        String accessToken = jwtService.generateToken(new CustomUserDetails(user));
        String refreshToken = generateRefreshToken();

        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash(generateRefreshToken())
                .expiresAt(OffsetDateTime.ofInstant(Instant.now().plusMillis(refreshExpiration), ZoneOffset.UTC))
                .build();
        sessionRepository.save(session);

        String redirectUrl = appUrl + "/oauth2/redirect?token=" + accessToken + "&refreshToken=" + refreshToken;
        log.info("OAuth2 login success for user: {}", user.getEmail());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "refresh_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}