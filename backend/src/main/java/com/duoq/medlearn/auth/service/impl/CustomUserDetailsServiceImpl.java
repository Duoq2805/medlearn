package com.duoq.medlearn.auth.service.impl;

import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        var user = userRepository.findByEmailWithRolesAndPermissions(usernameOrEmail)
                .orElseGet(() -> userRepository.findByUsernameWithRolesAndPermissions(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found")));
        return new CustomUserDetails(user);
    }
}