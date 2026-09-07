package com.duoq.medlearn.auth.config;

import com.duoq.medlearn.auth.entity.Role;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.auth.enums.UserStatus;
import com.duoq.medlearn.auth.repository.RoleRepository;
import com.duoq.medlearn.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevelopmentAdminInitializerTest {
    @Mock UserRepository users;
    @Mock RoleRepository roles;
    @Mock PasswordEncoder encoder;
    DevelopmentAdminInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DevelopmentAdminInitializer(users, roles, encoder);
        ReflectionTestUtils.setField(initializer, "email", " Admin@Example.COM ");
        ReflectionTestUtils.setField(initializer, "username", "dev-admin");
        ReflectionTestUtils.setField(initializer, "password", "test-only-value");
    }

    @Test
    void createsActiveVerifiedAdminUsingExistingRole() {
        Role role = Role.builder().name("ADMIN").build();
        when(users.findByEmailAndDeletedAtIsNull("admin@example.com")).thenReturn(Optional.empty());
        when(roles.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(encoder.encode("test-only-value")).thenReturn("encoded");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User created = captor.getValue();
        assertThat(created.getEmail()).isEqualTo("admin@example.com");
        assertThat(created.getUsername()).isEqualTo("dev-admin");
        assertThat(created.getPasswordHash()).isEqualTo("encoded");
        assertThat(created.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(created.getIsVerified()).isTrue();
        assertThat(created.getRoles()).containsExactly(role);
        verify(roles).findByName("ADMIN");
    }

    @Test
    void existingUserIsNotMutatedOrRecreated() {
        User existing = User.builder().email("admin@example.com").username("old").passwordHash("existing").status(UserStatus.PENDING).isVerified(false).build();
        when(users.findByEmailAndDeletedAtIsNull("admin@example.com")).thenReturn(Optional.of(existing));

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(users, never()).save(any());
        verifyNoInteractions(roles, encoder);
        assertThat(existing.getPasswordHash()).isEqualTo("existing");
        assertThat(existing.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(existing.getIsVerified()).isFalse();
    }

    @Test
    void allEmptyCredentialsDisableBootstrap() {
        ReflectionTestUtils.setField(initializer, "email", "");
        ReflectionTestUtils.setField(initializer, "username", "");
        ReflectionTestUtils.setField(initializer, "password", "");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(users, roles, encoder);
    }

    @Test
    void partialCredentialsFailBeforeDatabaseAccess() {
        ReflectionTestUtils.setField(initializer, "password", "");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments(new String[0])))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(users, roles, encoder);
    }
}
