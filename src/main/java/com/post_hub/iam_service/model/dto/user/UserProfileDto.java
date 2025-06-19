package com.post_hub.iam_service.model.dto.user;

import com.post_hub.iam_service.model.dto.role.RoleDto;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UserProfileDto implements Serializable {
    private Integer id;
    private String email;
    private String username;

    private RegistrationStatus registrationStatus;
    private LocalDateTime lastLogin;

    private String token;
    private String refreshToken;
    private List<RoleDto> roles;

}
