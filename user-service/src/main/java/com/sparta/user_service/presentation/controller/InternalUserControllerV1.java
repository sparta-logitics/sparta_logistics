package com.sparta.user_service.presentation.controller;

import com.example.sparta.common.exception.ErrorCode;
import com.sparta.user_service.domain.entity.UserEntity;
import com.sparta.user_service.domain.repository.UserRepository;
import com.sparta.user_service.presentation.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/internal/users")
public class InternalUserControllerV1 {

    private final UserRepository userRepository;

    @GetMapping("/by-username")
    public UserInfoResponse findByUsername(@RequestParam String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage()));
        return UserInfoResponse.of(user);
    }
}