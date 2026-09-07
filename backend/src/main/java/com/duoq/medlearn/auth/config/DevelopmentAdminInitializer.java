package com.duoq.medlearn.auth.config;

import com.duoq.medlearn.auth.entity.Role;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.auth.enums.UserStatus;
import com.duoq.medlearn.auth.repository.RoleRepository;
import com.duoq.medlearn.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
@Profile("docker")
@RequiredArgsConstructor
@Slf4j
public class DevelopmentAdminInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${medlearn.dev-admin.email:}") private String email;
    @Value("${medlearn.dev-admin.username:}") private String username;
    @Value("${medlearn.dev-admin.password:}") private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && username.isBlank() && password.isBlank()) {
            log.info("Development admin bootstrap disabled: credentials not configured.");
            return;
        }
        if (email.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Development admin bootstrap requires email, username, and password.");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailAndDeletedAtIsNull(email).isPresent()) {
            log.info("Development admin account already exists.");
            return;
        }
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role is not seeded."));
        User user = User.builder().username(username).email(email)
                .passwordHash(passwordEncoder.encode(password)).isVerified(true)
                .status(UserStatus.ACTIVE).roles(new HashSet<>(Set.of(adminRole))).build();
        userRepository.save(user);
        log.info("Development admin account provisioned.");
    }
}