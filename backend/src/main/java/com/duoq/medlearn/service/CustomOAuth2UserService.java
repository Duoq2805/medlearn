package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.UserStatus;
import com.duoq.medlearn.repository.RoleRepository;
import com.duoq.medlearn.repository.UserRepository;
import com.duoq.medlearn.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Lấy thông tin từ provider
        String email = extractEmail(provider, attributes);
        String name = extractName(provider, attributes);
        String providerId = extractProviderId(provider, attributes);

        log.info("OAuth2 login: provider={}, email={}, name={}", provider, email, name);

        // Kiểm tra user đã tồn tại chưa
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Cập nhật providerId nếu chưa có
            if (user.getProviderId() == null) {
                user.setProviderId(providerId);
                user.setProvider(provider);
                userRepository.save(user);
            }
        } else {
            // Tạo user mới
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));

            // Tạo username từ email (bỏ @ và dấu chấm)
            String username = generateUsernameFromEmail(email);

            user = User.builder()
                    .username(username)
                    .email(email)
                    .fullName(name)
                    .status(UserStatus.ACTIVE)  // OAuth2 users are auto-verified
                    .isVerified(true)            // Email đã được provider xác nhận
                    .provider(provider)
                    .providerId(providerId)
                    .roles(new HashSet<>(Set.of(userRole)))
                    .build();

            user = userRepository.save(user);
            log.info("New OAuth2 user created: {}", email);
        }

        return new CustomOAuth2User(user, attributes);
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        }
        if ("facebook".equals(provider)) {
            return (String) attributes.get("email");
        }
        return null;
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("email");
        }
        if ("facebook".equals(provider)) {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("email");
        }
        return (String) attributes.get("email");
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        }
        if ("facebook".equals(provider)) {
            return (String) attributes.get("id");
        }
        return (String) attributes.get("id");
    }

    private String generateUsernameFromEmail(String email) {
        String base = email.split("@")[0];
        // Loại bỏ các ký tự đặc biệt
        base = base.replaceAll("[^a-zA-Z0-9]", "");
        // Đảm bảo username không quá dài
        if (base.length() > 90) {
            base = base.substring(0, 90);
        }

        // Kiểm tra username đã tồn tại chưa
        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + counter;
            counter++;
        }
        return username;
    }
}