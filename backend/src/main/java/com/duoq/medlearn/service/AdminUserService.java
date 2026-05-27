package com.duoq.medlearn.service;

import com.duoq.medlearn.dto.response.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserDTO> getAllUsers(Pageable pageable);

    UserDTO getUserById(Long id);

    void deactivateUser(Long id);

    void activateUser(Long id);

    void assignRole(Long userId, String roleName);

    void removeRole(Long userId, String roleName);
}
