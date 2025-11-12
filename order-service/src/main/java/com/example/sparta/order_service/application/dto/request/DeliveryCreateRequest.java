package com.example.sparta.order_service.application.dto.request;

import com.example.sparta.order_service.presentation.dto.response.OrderLineResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DeliveryCreateRequest(
        UUID orderId,
        String destinationAddress,
        String recipientName,
        String recipientSlackId,
        UUID originHubId,
        UUID destinationHubId,
        List<OrderLineResponse> orderLines
) {
}
