package com.sparta.auth_service.presentation.response;

import com.example.sparta.common.enums.UserRoleEnum;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {
    private UUID userId;
    private String username;
    private UserRoleEnum role;
    private boolean valid;
    private String token;

}