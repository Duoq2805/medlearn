package com.duoq.medlearn.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "roleName is required")
    private String roleName;
}