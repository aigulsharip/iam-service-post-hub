package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.dto.user.UserDto;
import com.post_hub.iam_service.model.dto.user.UserSearchDto;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.request.user.UserSearchRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    IamResponse<UserDto> getById(@NotNull Integer id);

    IamResponse<UserDto> createUser(@NotNull NewUserRequest request);

    IamResponse<UserDto> updateUser(@NotNull Integer id, @NotNull UpdateUserRequest request);

    void softDeleteUser(@NotNull Integer id);

    IamResponse<PaginationResponse<UserSearchDto>> findAllUsers(Pageable pageable);

    IamResponse<PaginationResponse<UserSearchDto>> searchUsers (UserSearchRequest request, Pageable pageable);


}
