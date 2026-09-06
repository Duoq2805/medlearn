package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.user.UpdateProfileRequest;
import com.duoq.medlearn.domain.dto.user.UserResponse;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);
}
