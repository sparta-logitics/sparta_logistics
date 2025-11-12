package com.example.sparta.order_service.presentation.dto.request;

import com.example.sparta.order_service.domain.entity.Order;
import com.example.sparta.order_service.domain.entity.OrderLine;
import com.example.sparta.order_service.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record OrderRequest(
        String deliveryMessage,
        // TODO slackId 저장 로직 고려
        String slackId,
        @NotNull LocalDateTime dueDate,
        @NotNull(message = "주문 정보는 Null일 수 없습니다.") ShippingInfoRequest originInfo,
        @NotNull ShippingInfoRequest recipientInfo,
        @NotNull List<OrderLineRequest> orderLines) {

    public Order toEntity() {
        List<OrderLine> orderLineEntities = orderLines.stream()
                .map(OrderLineRequest::toEntity)
                .toList();
        long totalAmount = orderLineEntities.stream()
                .mapToLong(OrderLine::getAmounts)
                .sum();

        Order order = Order.builder()
                .deliveryMessage(deliveryMessage)
                .originInfo(originInfo.toEntity())
                .recipientInfo(recipientInfo.toEntity())
                .orderLines(orderLineEntities)
                .status(OrderStatus.PAYMENT_PENDING)
                .totalAmount(totalAmount)
                .dueDate(dueDate)
                .representativeProductName(orderLines.get(0).productName())
                .orderLineCount(orderLines.size())
                .build();

        orderLineEntities.forEach(orderLine -> orderLine.setOrderToCreate(order));

        return order;
    }
}
