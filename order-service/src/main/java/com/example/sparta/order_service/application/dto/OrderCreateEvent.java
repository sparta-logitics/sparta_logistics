package com.example.sparta.order_service.application.dto;

import com.example.sparta.order_service.domain.entity.ShippingInfo;

import java.util.UUID;

public record OrderCreateEvent(
        UUID orderId,
        ShippingInfo recipientInfo
) {
}
