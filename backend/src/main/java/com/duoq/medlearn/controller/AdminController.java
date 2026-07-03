package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.common.PagedResponse;
import com.duoq.medlearn.domain.dto.user.RoleRequest;
import com.duoq.medlearn.domain.dto.user.UserDTO;
import com.duoq.medlearn.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin user and system management")
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @PreAuthorize("@permissionService.hasPermission('USER_VIEW_ALL')")
    @Operation(summary = "List all users")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(adminUserService.getAllUsers(pageable))));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("@permissionService.hasPermission('USER_VIEW_ALL')")
    @Operation(summary = "Get user by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getUserById(id)));
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("@permissionService.hasPermission('ROLE_ASSIGN')")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(@PathVariable Long userId,
                                                             @Valid @RequestBody RoleRequest request) {
        adminUserService.assignRole(userId, request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success("Role assigned", null));
    }

    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("@permissionService.hasPermission('USER_MANAGE')")
    @Operation(summary = "Activate or deactivate a user")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long userId,
                                                               @RequestBody Map<String, Boolean> body) {
        boolean isActive = body.getOrDefault("isActive", false);
        if (isActive) {
            adminUserService.activateUser(userId);
        } else {
            adminUserService.deactivateUser(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(isActive ? "User activated" : "User deactivated", null));
    }

    @GetMapping("/analytics")
    @PreAuthorize("@permissionService.hasPermission('USER_VIEW_ALL')")
    @Operation(summary = "Get system analytics", description = "Placeholder for system statistics")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        Map<String, Object> analytics = Map.of(
                "message", "Analytics endpoint. Extend with actual statistics as needed."
        );
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/pending-reviews")
    @PreAuthorize("@permissionService.hasPermission('VERSION_REVIEW')")
    @Operation(summary = "Get pending reviews summary", description = "See GET /api/versions/pending-review for full paginated list")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<String>> getPendingReviews() {
        return ResponseEntity.ok(ApiResponse.success("See GET /api/versions/pending-review for full list"));
    }
}
