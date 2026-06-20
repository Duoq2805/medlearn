package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.auth.ForgotPasswordRequest;
import com.duoq.medlearn.domain.dto.auth.LoginRequest;
import com.duoq.medlearn.domain.dto.auth.RegisterRequest;
import com.duoq.medlearn.domain.dto.auth.ResendVerificationRequest;
import com.duoq.medlearn.domain.dto.auth.ResetPasswordRequest;
import com.duoq.medlearn.domain.dto.auth.AuthResponse;
import com.duoq.medlearn.domain.dto.auth.MessageResponse;
import com.duoq.medlearn.domain.dto.user.UserDTO;

public interface AuthService {
    MessageResponse register(RegisterRequest request);
    MessageResponse verifyEmail(String token);
    MessageResponse resendVerification(ResendVerificationRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken, String accessToken);
    UserDTO getCurrentUser();
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
}
