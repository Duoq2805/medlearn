package com.duoq.medlearn.service;

import com.duoq.medlearn.dto.request.UpdateProfileRequest;
import com.duoq.medlearn.dto.UserDTO;

public interface UserService {

    UserDTO getCurrentUser();

    UserDTO updateProfile(UpdateProfileRequest request);
}
