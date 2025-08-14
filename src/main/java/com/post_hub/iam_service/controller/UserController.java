package com.post_hub.iam_service.controller;


import com.post_hub.iam_service.model.constants.ApiLogMessage;
import com.post_hub.iam_service.model.dto.post.PostDTO;
import com.post_hub.iam_service.model.dto.user.UserDto;
import com.post_hub.iam_service.model.dto.user.UserSearchDto;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.request.user.NewUserRequest;
import com.post_hub.iam_service.model.request.user.UpdateUserRequest;
import com.post_hub.iam_service.model.request.user.UserSearchRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import com.post_hub.iam_service.service.UserService;
import com.post_hub.iam_service.utils.ApiUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("${end.point.users}")
public class UserController {

    private final UserService userService;

    @GetMapping("${end.point.id}")
    public ResponseEntity<IamResponse<UserDto>> getUserById(@NotNull @PathVariable("id") Integer id) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserDto> response = userService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.create}")
    public ResponseEntity<IamResponse<UserDto>> createUser(@NotNull @Valid @RequestBody NewUserRequest request) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserDto> response = userService.createUser(request);
        return ResponseEntity.ok(response);

    }

    @PutMapping("${end.point.id}")
    public ResponseEntity<IamResponse<UserDto>> updateUser (@NotNull @PathVariable("id") Integer id,
                                                            @NotNull @Valid @RequestBody UpdateUserRequest request) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<UserDto> response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("${end.point.id}")
    public ResponseEntity<Void> deleteUser(@PathVariable (name = "id") Integer userId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        userService.softDeleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("${end.point.all}")
    public ResponseEntity<IamResponse<PaginationResponse<UserSearchDto>>> getAllUsers (
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit",defaultValue = "10") int limit
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        Pageable pageable = PageRequest.of(page, limit);

        IamResponse<PaginationResponse<UserSearchDto>> response = userService.findAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

   @PostMapping("${end.points.search}")
    public ResponseEntity<IamResponse<PaginationResponse<UserSearchDto>>> searchUsers(
            @RequestBody @Valid UserSearchRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        IamResponse<PaginationResponse<UserSearchDto>> response = userService.searchUsers(request, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("${end.points.info}")
    @Operation(summary = "Get user info", description = "Get user info")
    public ResponseEntity<IamResponse<UserDto>> getUserData(Principal principal) {
        IamResponse<UserDto> response = userService.getUserInfo(principal.getName());
        return ResponseEntity.ok(response);
    }




}
