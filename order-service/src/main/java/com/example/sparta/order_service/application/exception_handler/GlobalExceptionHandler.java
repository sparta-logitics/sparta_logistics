package com.example.sparta.order_service.application.exception_handler;

import com.example.sparta.common.dto.ErrorResponse;
import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import com.example.sparta.order_service.domain.exception.OrderStatusException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e, HttpServletRequest request) {

        ErrorCode errorCode = e.getErrorCode();
        String requestURI = request.getRequestURI();

        if (errorCode.isClientError())
            log.warn("[Order Service] 비즈니스 예외 발생 - 코드: {}, 메시지: {}, 경로: {}",
                    errorCode.getCode(), e.getMessage(), requestURI);
        else
            log.error("[Order Service] 비즈니스 예외 발생 - 코드: {}, 메시지: {}, 경로: {}",
                    errorCode.getCode(), e.getMessage(), requestURI);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage(), requestURI));
    }

    @ExceptionHandler(OrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleOrderStatusException(
            OrderStatusException e, HttpServletRequest request) {

        ErrorCode errorCode = e.getErrorCode();
        String requestURI = request.getRequestURI();

        if (errorCode.isClientError())
            log.warn("[Order Service] 주문 상태 예외 발생 - 코드: {}, 메시지: {}, 경로: {}",
                    errorCode.getCode(), e.getMessage(), requestURI);
        else
            log.error("[Order Service] 주문 상태 예외 발생 - 코드: {}, 메시지: {}, 경로: {}",
                    errorCode.getCode(), e.getMessage(), requestURI);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage(), requestURI));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = e.getFieldErrors().stream()
                .map(fieldError -> ErrorResponse.FieldError.of(fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage()))
                .toList();

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String requestURI = request.getRequestURI();

        if (errorCode.isClientError())
            log.warn("[Order Service] 데이터 유효성 예외 발생 - 코드: {}, 메시지: {}, 경로: {}",
                    errorCode.getCode(), e.getMessage(), requestURI);
        else
            log.error("[Order Service] 데이터 유효성 예외 발생 - 코드: {}, 메시지: {}, 경로: {}",
                    errorCode.getCode(), e.getMessage(), requestURI);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, requestURI, fieldErrors));
    }
}
