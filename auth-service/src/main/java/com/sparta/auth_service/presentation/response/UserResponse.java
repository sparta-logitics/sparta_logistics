package com.sparta.auth_service.presentation.response;


import com.example.sparta.common.enums.UserRoleEnum;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {
    private UUID userId;
    private String username;
    private String password;
    private UserRoleEnum role;
    private boolean approved;
}