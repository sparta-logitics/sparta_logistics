package com.sparta.auth_service.infrastructure.config;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class UserClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // user-service에서 404가 오면 BusinessException으로 변환
        if (response.status() == 404) {
            return new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 그 외는 기본 처리
        return defaultDecoder.decode(methodKey, response);
    }
}