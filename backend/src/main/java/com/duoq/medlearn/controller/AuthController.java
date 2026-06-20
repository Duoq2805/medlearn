package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.auth.ForgotPasswordRequest;
import com.duoq.medlearn.domain.dto.auth.LoginRequest;
import com.duoq.medlearn.domain.dto.auth.RefreshTokenRequest;
import com.duoq.medlearn.domain.dto.auth.RegisterRequest;
import com.duoq.medlearn.domain.dto.auth.ResendVerificationRequest;
import com.duoq.medlearn.domain.dto.auth.ResetPasswordRequest;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.auth.AuthResponse;
import com.duoq.medlearn.domain.dto.user.UserDTO;
import com.duoq.medlearn.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        String msg = authService.register(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        String msg = authService.verifyEmail(token).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        String msg = authService.resendVerification(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String msg = authService.forgotPassword(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String msg = authService.resetPassword(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getMe() {
        return ResponseEntity.ok(ApiResponse.success("Get profile success", authService.getCurrentUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request.getRefreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest, @Valid @RequestBody RefreshTokenRequest request) {
        String authHeader = httpRequest.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        authService.logout(request.getRefreshToken(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}