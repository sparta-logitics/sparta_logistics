package com.example.sparta.api_gateway.exception;

public record ErrorResponse(int status, String message, String path) {
    public static ErrorResponse of(ErrorCode code, String path) {
        return new ErrorResponse(code.getStatusCode(), code.getMessage(), path);
    }
}