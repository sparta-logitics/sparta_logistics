package com.example.sparta.order_service.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID deliveryId,
        UUID orderId,
        // TODO DeliveryStatus 필요
        String status,
        LocalDateTime estimatedArrivalTime,
        Boolean isFail
) {
}
