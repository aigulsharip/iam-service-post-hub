package com.post_hub.iam_service.mapper;

import com.post_hub.iam_service.model.dto.role.RoleDto;
import com.post_hub.iam_service.model.dto.user.UserDto;
import com.post_hub.iam_service.model.dto.user.UserProfileDto;
import com.post_hub.iam_service.model.dto.user.UserSearchDto;
import com.post_hub.iam_service.model.entity.Role;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.RegistrationUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collection;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {RegistrationStatus.class, Object.class}
)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "registrationStatus", expression = "java(RegistrationStatus.ACTIVE)")
    User createUser(NewUserRequest newUserRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    User updateUser(@MappingTarget User user, UpdateUserRequest updateUserRequest);

    @Mapping(source = "deleted", target = "isDeleted")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserSearchDto toUserSearchDto(User user);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "token", source = "token")
    @Mapping(target = "refreshToken", source = "refreshToken")
    UserProfileDto toUserProfileDto(User user, String token, String refreshToken);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "registrationStatus", expression = "java(RegistrationStatus.ACTIVE)")
    User fromDto(RegistrationUserRequest registrationUserRequest);

    default List<RoleDto> mapRoles(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new RoleDto(role.getId(), role.getName()))
                .toList();
    }
}
