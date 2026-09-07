package com.duoq.medlearn.auth.service;

import com.duoq.medlearn.auth.dto.request.UpdateProfileRequest;
import com.duoq.medlearn.auth.dto.response.UserResponse;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);
}
