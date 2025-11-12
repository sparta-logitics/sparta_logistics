package com.example.sparta.order_service.application.event;

import com.example.sparta.order_service.application.dto.request.DeliveryCreateRequest;
import com.example.sparta.order_service.presentation.dto.response.OrderLineResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OrderCreateEvent(
        UUID orderId,
        String destinationAddress,
        String recipientName,
        String recipientSlackId,
        UUID originHubId,
        UUID destinationHubId,
        List<OrderLineResponse> orderLines
) {
    public DeliveryCreateRequest toRequest() {
        return DeliveryCreateRequest.builder()
                .orderId(orderId)
                .destinationAddress(destinationAddress)
                .recipientName(recipientName)
                .recipientSlackId(recipientSlackId)
                .originHubId(originHubId)
                .destinationHubId(destinationHubId)
                .orderLines(orderLines)
                .build();
    }
}
