package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.auth.AuthResponse;
import com.duoq.medlearn.domain.dto.auth.ForgotPasswordRequest;
import com.duoq.medlearn.domain.dto.auth.LoginRequest;
import com.duoq.medlearn.domain.dto.auth.RefreshTokenRequest;
import com.duoq.medlearn.domain.dto.auth.RegisterRequest;
import com.duoq.medlearn.domain.dto.auth.ResendVerificationRequest;
import com.duoq.medlearn.domain.dto.auth.ResetPasswordRequest;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.user.UserResponse;
import com.duoq.medlearn.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication, token, and account recovery APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new account")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        String msg = authService.register(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @GetMapping("/verify-email/{token}")
    @Operation(summary = "Verify email by path token")
    public ResponseEntity<ApiResponse<Void>> verifyEmailByPath(@PathVariable String token) {
        String msg = authService.verifyEmail(token).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email by query token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        String msg = authService.verifyEmail(token).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        String msg = authService.resendVerification(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String msg = authService.forgotPassword(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String msg = authService.resetPassword(request).getMessage();
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        return ResponseEntity.ok(ApiResponse.success("Get profile success", authService.getCurrentUser()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request.getRefreshToken())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke tokens")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String accessToken = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : null;
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(refreshToken, accessToken);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
