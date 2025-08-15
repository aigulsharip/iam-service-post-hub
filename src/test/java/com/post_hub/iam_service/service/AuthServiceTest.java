package com.post_hub.iam_service.service;

import com.post_hub.iam_service.mapper.UserMapper;
import com.post_hub.iam_service.model.dto.user.LoginRequest;
import com.post_hub.iam_service.model.dto.user.UserProfileDto;
import com.post_hub.iam_service.model.entity.RefreshToken;
import com.post_hub.iam_service.model.entity.Role;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.IamServiceUserRole;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.InvalidDataException;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.repository.RoleRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.security.JwtTokenProvider;
import com.post_hub.iam_service.security.validation.AccessValidator;
import com.post_hub.iam_service.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AccessValidator accessValidator;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserProfileDto testUserProfileDTO;
    private RefreshToken testRefreshToken;
    private Role userRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("TestUser");
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("hashedPassword");
        testUser.setRegistrationStatus(RegistrationStatus.ACTIVE);
        testUser.setLastLogin(LocalDateTime.now());

        userRole = new Role();
        userRole.setName("USER");
        testUser.setRoles(Collections.singleton(userRole));

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("refresh_token_123");
        testRefreshToken.setUser(testUser);

        testUserProfileDTO = new UserProfileDto(
                testUser.getId(),
                testUser.getUsername(),
                testUser.getEmail(),
                testUser.getRegistrationStatus(),
                testUser.getLastLogin(),
                "access_token_123",
                testRefreshToken.getToken(),
                Collections.emptyList()
        );
    }


    @Test
    void login_ValidCredentials_ReturnsUserProfile() {
        LoginRequest request = new LoginRequest("test@gmail.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findUserByEmailAndDeletedFalse(request.getEmail())).thenReturn(Optional.of(testUser));
        when(refreshTokenService.generateOrUpdateRefreshToken(testUser)).thenReturn(testRefreshToken);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("access_token_123");
        when(userMapper.toUserProfileDto(testUser, "access_token_123", testRefreshToken.getToken()))
                .thenReturn(testUserProfileDTO);

        IamResponse<UserProfileDto> result = authService.login(request);

        assertNotNull(result);
        assertEquals("access_token_123", result.getPayload().getToken());
        assertEquals("refresh_token_123", result.getPayload().getRefreshToken());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findUserByEmailAndDeletedFalse(request.getEmail());
        verify(refreshTokenService, times(1)).generateOrUpdateRefreshToken(testUser);
        verify(jwtTokenProvider, times(1)).generateToken(testUser);
        verify(userMapper, times(1)).toUserProfileDto(testUser, "access_token_123", testRefreshToken.getToken());
    }

//    @Test
//    void login_InvalidCredentials_ThrowsException() {
//        LoginRequest request = new LoginRequest("test@gmail.com", "wrongPassword");
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenThrow(new BadCredentialsException("Invalid credentials"));
//        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> authService.login(request));
//
//        assertTrue(exception.getMessage().contains("Invalid"));
//
//        verify(userRepository, never()).findUserByEmailAndDeletedFalse(request.getEmail());
//        verify(refreshTokenService, never()).generateOrUpdateRefreshToken(any(User.class));
//        verify(jwtTokenProvider, never()).generateToken(any(User.class));
//    }

//    @Test
//    void registerUser_ValidRequest_CreatesUserSuccessfully() {
//        RegistrationUserRequest request = new RegistrationUserRequest(
//                "newUser",
//                "newuser@gmail.com",
//                "password123!",
//                "password123!"
//        );
//
//        doNothing().when(accessValidator).validateNewUser(
//                request.getUsername(),
//                request.getEmail(),
//                request.getPassword(),
//                request.getConfirmPassword()
//        );
//
//        when(roleRepository.findByName(IamServiceUserRole.USER.getRole())).thenReturn(Optional.of(userRole));
//        when(userMapper.fromDto(request)).thenReturn(testUser);
//        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
//        when(userRepository.save(any(User.class))).thenReturn(testUser);
//        when(refreshTokenService.generateOrUpdateRefreshToken(testUser)).thenReturn(testRefreshToken);
//        when(jwtTokenProvider.generateToken(testUser)).thenReturn("access_token_123");
//        when(userMapper.toUserProfileDto(testUser, "access_token_123", testRefreshToken.getToken()))
//                .thenReturn(testUserProfileDTO);
//
//        IamResponse<String> result = authService.registerUser(request);
//
//        assertNotNull(result);
//        assertEquals("access_token_123", result.getPayload().getToken());
//        assertEquals("refresh_token_123", result.getPayload().getRefreshToken());
//
//        verify(accessValidator, times(1)).validateNewUser(
//                request.getUsername(),
//                request.getEmail(),
//                request.getPassword(),
//                request.getConfirmPassword()
//        );
//        verify(roleRepository, times(1)).findByName(IamServiceUserRole.USER.getRole());
//        verify(userRepository, times(1)).save(any(User.class));
//        verify(refreshTokenService, times(1)).generateOrUpdateRefreshToken(testUser);
//        verify(jwtTokenProvider, times(1)).generateToken(testUser);
//        verify(userMapper, times(1)).toUserProfileDto(testUser, "access_token_123", testRefreshToken.getToken());
//    }


}
