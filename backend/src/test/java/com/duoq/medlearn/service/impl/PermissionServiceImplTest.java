package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.*;
import com.duoq.medlearn.domain.enums.PermissionCode;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.security.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock private CurrentUserResolver currentUserResolver;
    @Mock private UserRepository userRepository;
    @InjectMocks private PermissionServiceImpl permissionService;

    private User userWithPerms;
    private User adminUser;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");
        Set<Permission> perms = new HashSet<>();
        perms.add(createPerm("VERSION_READ"));
        perms.add(createPerm("SECTION_WRITE"));
        userRole.setPermissions(perms);

        userWithPerms = new User();
        userWithPerms.setId(1L);
        userWithPerms.setUsername("user");
        userWithPerms.setRoles(new HashSet<>(Set.of(userRole)));

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");
        Set<Permission> adminPerms = new HashSet<>();
        for (PermissionCode pc : PermissionCode.values()) adminPerms.add(createPerm(pc.name()));
        adminRole.setPermissions(adminPerms);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRoles(new HashSet<>(Set.of(adminRole)));
    }

    private Permission createPerm(String name) { Permission p = new Permission(); p.setName(name); return p; }

    @Test void hasPermission_shouldReturnTrue_whenUserHasPermission() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(userWithPerms));
        assertThat(permissionService.hasPermission(PermissionCode.VERSION_READ)).isTrue();
    }

    @Test void hasPermission_shouldReturnFalse_whenUserLacksPermission() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(userWithPerms));
        assertThat(permissionService.hasPermission(PermissionCode.DISEASE_WRITE)).isFalse();
    }

    @Test void hasAnyPermission_shouldReturnTrue_whenUserHasAtLeastOne() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(userWithPerms));
        assertThat(permissionService.hasAnyPermission(PermissionCode.DISEASE_WRITE, PermissionCode.VERSION_READ)).isTrue();
    }

    @Test void hasAnyPermission_shouldReturnFalse_whenUserHasNone() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(userWithPerms));
        assertThat(permissionService.hasAnyPermission(PermissionCode.DISEASE_WRITE, PermissionCode.USER_MANAGE)).isFalse();
    }

    @Test void getCurrentUserPermissions_shouldReturnAllPermissions() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(userWithPerms));
        assertThat(permissionService.getCurrentUserPermissions()).containsExactlyInAnyOrder(PermissionCode.VERSION_READ, PermissionCode.SECTION_WRITE);
    }

    @Test void getCurrentUserPermissions_shouldReturnEmptySet_whenUserHasNoRoles() {
        User empty = new User(); empty.setId(3L); empty.setRoles(new HashSet<>());
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(3L);
        when(userRepository.findByIdWithRolesAndPermissions(3L)).thenReturn(Optional.of(empty));
        assertThat(permissionService.getCurrentUserPermissions()).isEmpty();
    }

    @Test void getCurrentUserPermissions_shouldThrow_whenUserNotFound() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(999L);
        when(userRepository.findByIdWithRolesAndPermissions(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> permissionService.getCurrentUserPermissions()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test void hasPermission_shouldReturnTrue_forAdminWithAllPermissions() {
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(2L);
        when(userRepository.findByIdWithRolesAndPermissions(2L)).thenReturn(Optional.of(adminUser));
        assertThat(permissionService.hasPermission(PermissionCode.DISEASE_WRITE)).isTrue();
        assertThat(permissionService.hasPermission(PermissionCode.USER_MANAGE)).isTrue();
    }
}
