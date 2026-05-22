package com.duoq.medlearn.service;

import com.duoq.medlearn.dto.request.LoginRequest;
import com.duoq.medlearn.dto.request.RegisterRequest;
import com.duoq.medlearn.dto.response.AuthResponse;
import com.duoq.medlearn.dto.response.MessageResponse;
import com.duoq.medlearn.dto.response.UserDTO;

public interface AuthService {
    MessageResponse register(RegisterRequest request);
    MessageResponse verifyEmail(String token);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    UserDTO getCurrentUser();

}
