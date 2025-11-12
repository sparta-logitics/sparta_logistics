package com.sparta.deliveryservice.dto.response;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeliverySummaryResponse {

    // 목록에 보여줄 핵심 요약 정보
    private UUID deliveryId;
    private UUID orderId;
    private DeliveryStatus status;
    private String recipientName;
    private String destinationAddress;
    private LocalDateTime estimatedArrivalTime;

    // 엔티티를 DTO로 변환하는 생성자
    public DeliverySummaryResponse(Delivery delivery) {
        this.deliveryId = delivery.getDeliveryId();
        this.orderId = delivery.getOrderId();
        this.status = delivery.getStatus();
        this.recipientName = delivery.getRecipientName();
        this.destinationAddress = delivery.getDestinationAddress();
        this.estimatedArrivalTime = delivery.getEstimatedArrivalTime();
    }
}
