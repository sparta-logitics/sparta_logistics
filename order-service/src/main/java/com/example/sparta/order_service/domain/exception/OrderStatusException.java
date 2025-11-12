package com.example.sparta.order_service.domain.exception;

import com.example.sparta.common.exception.BusinessException;
import com.example.sparta.common.exception.ErrorCode;

public class OrderStatusException extends BusinessException {
    public OrderStatusException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrderStatusException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public OrderStatusException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public OrderStatusException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
