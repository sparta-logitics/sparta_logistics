package com.example.sparta.api_gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            ServerWebExchange exchange
    ) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
                .status(code.getStatusCode())
                .body(ErrorResponse.of(code, exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            WebExchangeBindException e,
            ServerWebExchange exchange
    ) {
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatusCode())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e,
            ServerWebExchange exchange
    ) {
        return ResponseEntity
                .status(ErrorCode.INVALID_TYPE_VALUE.getStatusCode())
                .body(ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE, exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            ServerWebExchange exchange
    ) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatusCode())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, exchange.getRequest().getPath().value()));
    }
}