package com.sparta.user_service.presentation.handler;

import com.example.sparta.common.dto.ErrorResponse;
import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 단일 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e, HttpServletRequest request) {

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI()));
    }

    /**
     * @Valid 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e,
                                                       HttpServletRequest request) {

        var fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.of(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, request.getRequestURI(), fieldErrors));
    }

    /**
     * 타입 불일치 예외
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                HttpServletRequest request) {

        return ResponseEntity
                .status(ErrorCode.INVALID_TYPE_VALUE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE, request.getRequestURI()));
    }

    /**
     * 기타 예상 못한 모든 예외 잡기
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}
