package com.sparta.user_service.presentation.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserUpdateRequest {
    private String name;
    private String slackId;
}