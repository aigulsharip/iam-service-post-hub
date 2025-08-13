package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDto;
import com.post_hub.iam_service.model.entity.EmailVerificationToken;
import com.post_hub.iam_service.model.entity.RefreshToken;
import com.post_hub.iam_service.model.entity.Role;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.IamServiceUserRole;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.InvalidDataException;
import com.post_hub.iam_service.model.exception.InvalidPasswordException;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.security.JwtTokenProvider;
import com.post_hub.iam_service.security.validation.AccessValidator;
import com.post_hub.iam_service.service.AuthService;
import com.post_hub.iam_service.service.MailSenderService;
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
    private final AccessValidator accessValidator;
    private final MailSenderService mailSenderService;

    @Override
    public IamResponse<UserProfileDto> login(LoginRequest request) {
        User user = userRepository.findUserByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage()));

        if (user.getRegistrationStatus() != RegistrationStatus.ACTIVE) {
            throw new InvalidDataException(ApiErrorMessage.CONFIRM_YOUR_EMAIL.getMessage());
        }

        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidDataException(ApiErrorMessage.INVALID_USER_OR_PASSWORD.getMessage());
        }

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
    public IamResponse<String> registerUser(@NonNull RegistrationUserRequest request) {
        accessValidator.validateNewUser(request.getUsername(), request.getEmail(), request.getPassword(), request.getConfirmPassword());
        Role userRole = roleRepository.findByName(IamServiceUserRole.USER.getRole())
                .orElseThrow(() -> new DataExistException(ApiErrorMessage.USER_ROLE_NOT_FOUND.getMessage()));

        User newUser =userMapper.fromDto(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        userRepository.save(newUser);

        EmailVerificationToken token = mailSenderService.createToken(newUser);
        mailSenderService.sendVerificationEmail(newUser.getEmail(), token.getToken());

        return IamResponse.createSuccessful("Registration successful. Please check your email to confirm your account");


    }
}
