package com.duoq.medlearn.config;

import com.duoq.medlearn.security.JwtAuthenticationFilter;
import com.duoq.medlearn.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    // ===== CHAIN 1: API Security (JWT) - Order cao nhất =====
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")  // Chỉ áp dụng cho API endpoints
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    // ===== PUBLIC AUTH ENDPOINTS =====
                    auth.requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login",
                            "/api/auth/verify",
                            "/api/auth/verify-email/**",
                            "/api/auth/refresh",
                            "/api/auth/forgot-password",
                            "/api/auth/reset-password",
                            "/api/auth/resend-verification"
                    ).permitAll();
                    // ===== PUBLIC GET ENDPOINTS (read-only) =====
                    auth.requestMatchers(
                            HttpMethod.GET,
                            "/api/diseases/**",
                            "/api/categories/**",
                            "/api/symptoms",
                            "/api/symptoms/search",
                            "/api/cases",
                            "/api/cases/**",
                            "/api/notifications/**",
                            "/api/ai/**",
                            "/api/versions/**",
                            "/api/sections/**"
                    ).permitAll();
                    // ===== PUBLIC POST ENDPOINTS (no auth needed) =====
                    auth.requestMatchers(HttpMethod.POST, "/api/symptom-checker/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/diseases/search").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/cases/{id}/diagnose").permitAll();
                    // ===== PROTECTED: auth required =====
                    auth.requestMatchers("/api/auth/me", "/api/auth/logout").authenticated();
                    // ===== ALL OTHER ENDPOINTS (write/moderation/admin): require auth =====
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ===== CHAIN 2: OAuth2 Security (Google/Facebook Login) =====
    @Bean
    @Order(2)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")  // Chỉ cho OAuth2 endpoints
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // OAuth2 endpoints đều public
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // ===== CHAIN 3: Default cho tất cả request còn lại =====
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Hoặc .authenticated() tùy nhu cầu
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}