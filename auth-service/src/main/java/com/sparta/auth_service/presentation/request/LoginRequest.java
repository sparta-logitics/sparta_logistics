package com.sparta.auth_service.presentation.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequest {
    private String username;
    private String password;
}