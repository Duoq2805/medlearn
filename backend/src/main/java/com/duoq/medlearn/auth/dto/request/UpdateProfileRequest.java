package com.duoq.medlearn.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;

    private String avatarUrl;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phoneNumber;
}
