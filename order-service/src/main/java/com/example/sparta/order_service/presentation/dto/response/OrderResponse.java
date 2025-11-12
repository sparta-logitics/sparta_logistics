package com.example.sparta.order_service.presentation.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        LocalDateTime orderDate,
        LocalDateTime dueDate,
        String companyName,
        String responsibility,
        String productName,
        Long totalAmount,
        String status
) {
    @QueryProjection
    public OrderResponse(UUID orderId, LocalDateTime orderDate, LocalDateTime dueDate, String companyName, String responsibility, String productName, Long totalAmount, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.dueDate = dueDate;
        this.companyName = companyName;
        this.responsibility = responsibility;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}
