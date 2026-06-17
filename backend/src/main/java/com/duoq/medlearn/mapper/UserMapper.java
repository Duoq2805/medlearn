package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.entity.Role;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.dto.auth.AuthResponse;
import com.duoq.medlearn.domain.dto.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoleNames(user.getRoles()))")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    UserDTO toUserDTO(User user);

    @Mapping(target = "roles", expression = "java(mapRoleNames(user.getRoles()))")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "tokenType", source = "tokenType")
    @Mapping(target = "expiresIn", source = "expiresIn")
    AuthResponse toAuthResponse(User user, String accessToken, String refreshToken, String tokenType, Long expiresIn);

    default Set<String> mapRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
