package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.user.UserDto;
import com.post_hub.iam_service.model.dto.user.UserSearchDto;
import com.post_hub.iam_service.model.entity.Role;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.IamServiceUserRole;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.request.user.UserSearchRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.repository.criteria.UserSearchCriteria;
import com.post_hub.iam_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

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

        Role userRole = roleRepository.findByName(IamServiceUserRole.USER.getRole())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.ROLE_NOT_FOUND_BY_NAME.getMessage()));

        User user = userMapper.createUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
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

    @Override
    public IamResponse<PaginationResponse<UserSearchDto>> findAllUsers(Pageable pageable) {
        Page<UserSearchDto> users = userRepository.findAll(pageable).map(userMapper::toUserSearchDto);

        PaginationResponse<UserSearchDto> paginationResponse = new PaginationResponse<>(
                users.getContent(),
                new PaginationResponse.Pagination(
                        users.getTotalElements(),
                        pageable.getPageSize(),
                        users.getNumber() + 1,
                        users.getTotalPages()));


        return IamResponse.createSuccessful(paginationResponse);
    }

    @Override
    public IamResponse<PaginationResponse<UserSearchDto>> searchUsers(UserSearchRequest request, Pageable pageable) {
        Specification<User> specification = new UserSearchCriteria(request);
        Page<UserSearchDto> users = userRepository.findAll(specification, pageable)
                .map(userMapper::toUserSearchDto);
        PaginationResponse<UserSearchDto> paginationResponse = PaginationResponse.<UserSearchDto>builder()
                .content(users.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(users.getTotalElements())
                        .page(users.getNumber() +1)
                        .limit(pageable.getPageSize())
                        .pages(users.getTotalPages())
                        .build()
                )
                .build();
        return IamResponse.createSuccessful(paginationResponse);
    }
}
