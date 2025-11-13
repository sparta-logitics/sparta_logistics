package com.example.sparta.api_gateway.exception;

public enum ErrorCode {
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(400, "타입이 맞지 않습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 에러");

    private final int statusCode;
    private final String message;

    ErrorCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}