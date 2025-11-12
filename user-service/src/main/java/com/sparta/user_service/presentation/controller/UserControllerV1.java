package com.sparta.user_service.presentation.controller;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.sparta.user_service.application.UserServiceV1;
import com.sparta.user_service.domain.entity.UserEntity;
import com.example.sparta.common.enums.UserRoleEnum;
import com.sparta.user_service.domain.enums.UserStatusEnum;
import com.sparta.user_service.presentation.request.UserCreateRequest;
import com.sparta.user_service.presentation.request.UserUpdateRequest;
import com.sparta.user_service.presentation.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserControllerV1 {

    private final UserServiceV1 userServiceV1;

    private final PagedResourcesAssembler<UserSearchResponse> assembler;

    // -------------------------
    // 생성, 수정, 삭제: MASTER 관리자만 가능
    // -------------------------
    @PostMapping()
    public ResponseEntity<UserCreateResponse> create(
            @RequestHeader("X-USER-ROLE") UserRoleEnum role,
            @RequestBody @Valid UserCreateRequest signupRequest
    ){
        if (role != UserRoleEnum.MASTER) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userServiceV1.create(signupRequest));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserUpdateResponse> updateUser(
            @RequestHeader("X-USER-ROLE") UserRoleEnum role,
            @PathVariable UUID userId,
            @RequestBody @Valid UserUpdateRequest request
    ) {
        if (role != UserRoleEnum.MASTER) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        UserEntity updatedUser = userServiceV1.updateUser(userId, request);
        UserUpdateResponse response = UserUpdateResponse.of(updatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader("X-USER-ROLE") UserRoleEnum role,
            @PathVariable UUID userId,
            @RequestParam(required = false) Long deletedBy // 삭제 요청자 ID (옵션)
    ) {
        if (role != UserRoleEnum.MASTER) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        userServiceV1.deleteUser(userId, deletedBy);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    // -------------------------
    // 회원가입 승인/거절: MASTER, HUB 관리자만 가능
    // -------------------------
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserApprovalResponse> changeStatus(
            @RequestHeader("X-USER-ROLE") UserRoleEnum role,
            @PathVariable UUID userId,
            @RequestParam UserStatusEnum status
    ){
        if (role != UserRoleEnum.MASTER && role != UserRoleEnum.HUB_MANAGER) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        UserEntity updatedUser = userServiceV1.changeStatus(userId, status);
        UserApprovalResponse response = UserApprovalResponse.of(updatedUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserRoleChangeResponse> changeRole(
            @RequestHeader("X-USER-ROLE") UserRoleEnum role,
            @PathVariable UUID userId,
            @RequestParam UserRoleEnum newRole
    ){
        if (role != UserRoleEnum.MASTER && role != UserRoleEnum.HUB_MANAGER) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        UserEntity updatedUser = userServiceV1.changeRole(userId, newRole);
        UserRoleChangeResponse response = UserRoleChangeResponse.of(updatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestHeader("X-USER-ROLE") UserRoleEnum role,
            @RequestHeader("X-USERID") UUID requestUserId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String slackId,
            @RequestParam(required = false) UserRoleEnum roleFilter,
            @RequestParam(required = false) UserStatusEnum status,
            Pageable pageable
    ) {
        Page<UserSearchResponse> page;
        if (role == UserRoleEnum.MASTER) {
            page = userServiceV1.searchUsers(name, slackId, roleFilter, status, pageable);
        } else {
            page = userServiceV1.searchUsersByUserId(requestUserId);
        }
        return ResponseEntity.ok(assembler.toModel(page));
    }


}
