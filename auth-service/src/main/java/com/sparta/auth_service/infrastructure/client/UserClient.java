package com.sparta.auth_service.infrastructure.client;

import com.sparta.auth_service.infrastructure.config.UserClientErrorDecoder;
import com.sparta.auth_service.presentation.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/v1/internal/users", configuration = UserClientErrorDecoder.class)
public interface UserClient {

    @GetMapping("/by-username")
    UserResponse getUserByUsername(@RequestParam("username") String username);
}