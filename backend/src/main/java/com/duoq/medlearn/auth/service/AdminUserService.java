package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    void deactivateUser(Long id);

    void activateUser(Long id);

    void assignRole(Long userId, String roleName);

    void removeRole(Long userId, String roleName);
}
