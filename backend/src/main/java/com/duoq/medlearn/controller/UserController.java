package com.duoq.medlearn.controller;

import com.duoq.medlearn.dto.request.RoleRequest;
import com.duoq.medlearn.dto.request.UpdateProfileRequest;
import com.duoq.medlearn.dto.response.ApiResponse;
import com.duoq.medlearn.dto.UserDTO;
import com.duoq.medlearn.service.AdminUserService;
import com.duoq.medlearn.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AdminUserService adminUserService;

    // ==================== USER PROFILE ====================

    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser()));
    }

    @PutMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(request)));
    }

    // ==================== ADMIN: USER MANAGEMENT ====================

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getAllUsers(pageable)));
    }

    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getUserById(id)));
    }

    @PatchMapping("/api/admin/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        adminUserService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @PatchMapping("/api/admin/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        adminUserService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated", null));
    }

    @PatchMapping("/api/admin/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        adminUserService.assignRole(id, request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success("Role assigned", null));
    }

    @DeleteMapping("/api/admin/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        adminUserService.removeRole(id, request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success("Role removed", null));
    }
}
