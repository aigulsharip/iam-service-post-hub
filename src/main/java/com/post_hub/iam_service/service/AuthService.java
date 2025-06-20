package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.dto.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDto;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;

public interface AuthService {
    IamResponse<UserProfileDto> login(LoginRequest request);

    IamResponse<UserProfileDto> refreshAccessToken(String refreshToken);

    IamResponse<UserProfileDto> registerUser(RegistrationUserRequest request);
}

