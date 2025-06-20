package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDto;
import com.post_hub.iam_service.model.entity.RefreshToken;
import com.post_hub.iam_service.model.entity.Role;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.IamServiceUserRole;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.InvalidDataException;
import com.post_hub.iam_service.model.exception.InvalidPasswordException;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.security.JwtTokenProvider;
import com.post_hub.iam_service.service.AuthService;
import com.post_hub.iam_service.service.RefreshTokenService;
import com.post_hub.iam_service.utils.PasswordUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider  jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IamResponse<UserProfileDto> login(LoginRequest request) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage());
        }
        User user = userRepository.findUserByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage()));

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(user);
        String token = jwtTokenProvider.generateToken(user);

        UserProfileDto userProfileDto = userMapper.toUserProfileDto(user, token, refreshToken.getToken());
        userProfileDto.setToken(token);

        return IamResponse.createSuccessfulWithNewToken(userProfileDto);
    }

    @Override
    public IamResponse<UserProfileDto> refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateAndRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        String accessToken = jwtTokenProvider.generateToken(user);

        return IamResponse.createSuccessfulWithNewToken(
                userMapper.toUserProfileDto(user, accessToken, refreshToken.getToken())
        );
    }

    @Override
    public IamResponse<UserProfileDto> registerUser(@NonNull RegistrationUserRequest request) {

        userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.USER_ALREADY_USERNAME_EXISTS.getMessage(request.getUsername()));
        });

        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            throw new DataExistException(ApiErrorMessage.USER_ALREADY_EMAIL_EXISTS.getMessage(request.getEmail()));
        });

        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!password.equals(confirmPassword)) {
            throw new InvalidPasswordException(ApiErrorMessage.MISMATCH_PASSWORDS.getMessage());
        }
        if (PasswordUtils.isNotValidPassword(password)) {
            throw new InvalidPasswordException(ApiErrorMessage.INVALID_PASSWORD.getMessage());
        }

        Role userRole = roleRepository.findByName(IamServiceUserRole.USER.getRole())
                .orElseThrow(() -> new DataExistException(ApiErrorMessage.ROLE_NOT_FOUND_BY_NAME.getMessage()));

        User newUser =userMapper.fromDto(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        userRepository.save(newUser);

        RefreshToken refreshToken = refreshTokenService.generateOrUpdateRefreshToken(newUser);
        String token = jwtTokenProvider.generateToken(newUser);
        UserProfileDto userProfileDto = userMapper.toUserProfileDto(newUser, token, refreshToken.getToken());
        userProfileDto.setToken(token);
        return IamResponse.createSuccessfulWithNewToken(userProfileDto);
    }
}
