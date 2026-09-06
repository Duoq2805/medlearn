package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @PreAuthorize("@permissionService.hasPermission('ROLE_ASSIGN')")
    public Role getRoleByName(String name) {
        return null;
    }

    @Override
    @PreAuthorize("@permissionService.hasPermission('ROLE_ASSIGN')")
    @Transactional
    public void assignRole(Long userId, String roleName) {

    }

    @Override
    @PreAuthorize("@permissionService.hasPermission('ROLE_ASSIGN')")
    @Transactional
    public void removeRole(Long userId, String roleName) {

    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        return false;
    }
}
