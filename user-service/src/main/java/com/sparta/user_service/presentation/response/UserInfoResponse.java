package com.sparta.user_service.presentation.response;

import com.example.sparta.common.enums.UserRoleEnum;
import com.sparta.user_service.domain.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserInfoResponse {
    private UUID userId;
    private String username;
    private String password;
    private UserRoleEnum role;
    private boolean approved;

    public static UserInfoResponse of(UserEntity user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .password(user.getPassword())
                .role(user.getRole())
                .approved(user.getStatus().name().equals("APPROVED"))
                .build();
    }
}