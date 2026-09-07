package com.duoq.medlearn.auth.service;

import com.duoq.medlearn.auth.dto.request.ForgotPasswordRequest;
import com.duoq.medlearn.auth.dto.request.LoginRequest;
import com.duoq.medlearn.auth.dto.request.RegisterRequest;
import com.duoq.medlearn.auth.dto.request.ResendVerificationRequest;
import com.duoq.medlearn.auth.dto.request.ResetPasswordRequest;
import com.duoq.medlearn.auth.dto.response.AuthResponse;
import com.duoq.medlearn.auth.dto.response.MessageResponse;
import com.duoq.medlearn.auth.dto.response.UserResponse;

public interface AuthService {
    MessageResponse register(RegisterRequest request);
    MessageResponse verifyEmail(String token);
    MessageResponse resendVerification(ResendVerificationRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken, String accessToken);
    UserResponse getCurrentUser();
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
}
