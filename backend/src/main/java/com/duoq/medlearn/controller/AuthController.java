package com.duoq.medlearn.controller;

import com.duoq.medlearn.dto.request.ForgotPasswordRequest;
import com.duoq.medlearn.dto.request.LoginRequest;
import com.duoq.medlearn.dto.request.RefreshTokenRequest;
import com.duoq.medlearn.dto.request.RegisterRequest;
import com.duoq.medlearn.dto.request.ResendVerificationRequest;
import com.duoq.medlearn.dto.request.ResetPasswordRequest;
import com.duoq.medlearn.dto.response.ApiResponse;
import com.duoq.medlearn.dto.response.AuthResponse;
import com.duoq.medlearn.dto.UserDTO;
import com.duoq.medlearn.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}