package com.sparta.auth_service.application;


import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.sparta.auth_service.infrastructure.client.UserClient;
import com.sparta.auth_service.presentation.response.LoginResponse;
import com.sparta.auth_service.presentation.response.UserResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.Key;
import java.time.Duration;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class AuthServiceV1 {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    public Mono<LoginResponse> login(String username, String password) {
        return Mono.fromCallable(() -> {
                    System.out.println("[1] Feign 호출 시작: " + username);
                    UserResponse user = userClient.getUserByUsername(username);
                    System.out.println("[2] Feign 호출 완료: " + user);
                    return user;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> {
                    System.out.println("[3] Password 검증 전: " + user.getUsername());
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        System.out.println("[4] Password 불일치");
                        return Mono.error(new BusinessException(ErrorCode.INVALID_CREDENTIALS));
                    }
                    System.out.println("[5] Password 일치");

                    if (!user.isApproved()) {
                        System.out.println("[6] 승인되지 않은 사용자");
                        return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED));
                    }

                    String token = Jwts.builder()
                            .claim("user_id", user.getUserId().toString())
                            .claim("username", user.getUsername())
                            .claim("role", user.getRole().name())
                            .setIssuedAt(new Date())
                            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                            .compact();

                    LoginResponse response = LoginResponse.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .role(user.getRole())
                            .token(token)
                            .valid(true)
                            .build();

                    System.out.println("[7] LoginResponse 생성 완료: " + response);
                    return Mono.just(response);
                })
                .timeout(Duration.ofSeconds(3))
                .onErrorMap(throwable -> {
                    System.out.println("[ERROR] 예외 발생: " + throwable);
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        return new BusinessException(ErrorCode.REQUEST_TIMEOUT);
                    }
                    return throwable;
                });
    }
}