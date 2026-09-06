package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;

public interface RoleService {
    Role getRoleByName(String name);

    void assignRole(Long userId, String roleName);

    void removeRole(Long userId, String roleName);

    boolean hasRole(Long userId, String roleName);
}
