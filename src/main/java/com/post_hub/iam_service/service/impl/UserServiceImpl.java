package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.post.PostDTO;
import com.post_hub.iam_service.model.dto.user.UserDto;
import com.post_hub.iam_service.model.entity.Post;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public IamResponse<UserDto> getById(Integer id) {
        User user = userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new EntityNotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));
        UserDto userDto = userMapper.toDto(user);
        return IamResponse.createSuccessful(userDto);
    }

    @Override
    public IamResponse<UserDto> createUser(NewUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DataExistException(ApiErrorMessage.USER_ALREADY_USERNAME_EXISTS.getMessage(request.getUsername()));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataExistException(ApiErrorMessage.USER_ALREADY_EMAIL_EXISTS.getMessage(request.getEmail()));
        }
        User user = userMapper.createUser(request);
        User savedUser = userRepository.save(user);
        UserDto userDto = userMapper.toDto(savedUser);
        return IamResponse.createSuccessful(userDto);
    }

    @Override
    public IamResponse<UserDto> updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));
        userMapper.updateUser(user, request);
        user.setUpdated(LocalDateTime.now());
        userRepository.save(user);

        UserDto userDto = userMapper.toDto(user);
        return IamResponse.createSuccessful(userDto);
    }

    @Override
    public void softDeleteUser(Integer id) {
        User user = userRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));
        user.setDeleted(true);
        userRepository.save(user);
    }

}
