package com.duoq.medlearn.auth.service;

import com.duoq.medlearn.auth.entity.Role;
import com.duoq.medlearn.auth.entity.User;

public interface RoleService {
    Role getRoleByName(String name);

    void assignRole(Long userId, String roleName);

    void removeRole(Long userId, String roleName);

    boolean hasRole(Long userId, String roleName);
}
