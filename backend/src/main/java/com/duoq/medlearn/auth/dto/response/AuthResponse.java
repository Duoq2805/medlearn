package com.duoq.medlearn.domain.dto.auth;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
}