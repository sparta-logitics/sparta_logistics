package com.sparta.deliveryservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice // 모든 @RestController의 예외를 가로챔
public class GlobalExceptionHandler {

    /**
     * [404 Not Found]
     * TDD에서 만든 'EntityNotFoundException'을 처리합니다.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> errorResponse = Map.of("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * [409 Conflict] 또는 [400 Bad Request]
     * TDD에서 만든 'IllegalStateException' (상태 불일치)을 처리합니다.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> errorResponse = Map.of("message", ex.getMessage());
        // 409 Conflict: 리소스의 현재 '상태'와 충돌 (예: 이미 시작됨)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * [500 Internal Server Error]
     * 처리하지 못한 모든 서버 내부 오류를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        // 실제 운영 시에는 log.error()로 에러 로그를 남겨야 한다
        Map<String,String> errorResponse = Map.of("message", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // 향후 Spring Security 추가 시 AccessDeniedException (403 Forbidden) 처리기 추가
}
