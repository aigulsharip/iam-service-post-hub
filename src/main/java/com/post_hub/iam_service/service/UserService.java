package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.dto.user.UserDto;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;

public interface UserService {
    IamResponse<UserDto> getById(@NotNull Integer id);

    IamResponse<UserDto> createUser(@NotNull NewUserRequest request);

    IamResponse<UserDto> updateUser(@NotNull Integer id, @NotNull UpdateUserRequest request);

    void softDeleteUser(@NotNull Integer id);
}
