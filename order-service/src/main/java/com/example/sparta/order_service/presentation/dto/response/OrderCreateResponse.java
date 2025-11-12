package com.example.sparta.order_service.presentation.dto.response;

import com.example.sparta.order_service.domain.entity.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderCreateResponse(UUID orderId,
                                  String deliveryMessage,
                                  Long totalAmount,
                                  LocalDateTime orderDate,
                                  String orderedBy,
                                  OrderStatus state,
                                  ShippingInfoResponse originInfo,
                                  ShippingInfoResponse recipientInfo,
                                  List<OrderLineResponse> orderLines) {
}
