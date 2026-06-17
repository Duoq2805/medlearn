package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.user.UpdateProfileRequest;
import com.duoq.medlearn.domain.dto.user.UserDTO;

public interface UserService {

    UserDTO getCurrentUser();

    UserDTO updateProfile(UpdateProfileRequest request);
}
