package com.sparta.user_service.presentation.response;

import com.sparta.user_service.domain.entity.UserEntity;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserUpdateResponse {

    private UUID userId;
    private String username;
    private String name;
    private String slackId;

    public static UserUpdateResponse of(UserEntity user) {
        UserUpdateResponse response = new UserUpdateResponse();
        response.userId = user.getUserId();
        response.username = user.getUsername();
        response.name = user.getName();
        response.slackId = user.getSlackId();
        return response;
    }
}
