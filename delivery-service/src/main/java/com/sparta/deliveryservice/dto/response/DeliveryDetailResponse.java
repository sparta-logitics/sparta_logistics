package com.sparta.deliveryservice.dto.response;

import com.sparta.deliveryservice.domain.Delivery;
import com.sparta.deliveryservice.domain.enums.DeliveryStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class DeliveryDetailResponse {

    // 클라이언트에 보여줄 배송 마스터 정보
    private UUID deliveryId;
    private UUID orderId;
    private DeliveryStatus status;
    private String destinationAddress;
    private String recipientName;
    private LocalDateTime estimatedArrivalTime;
    private LocalDateTime actualDeliveryTime;
    private UUID companyDriverId;

    // 내부에 자식 DTO 리스트 포함
    private List<RouteHistoryResponse> routeHistories;

    // 엔티티를 DTO로 변환하는 생성자

    public DeliveryDetailResponse(Delivery entity) {
        this.deliveryId = entity.getDeliveryId();
        this.orderId = entity.getOrderId();
        this.status = entity.getStatus();
        this.destinationAddress = entity.getDestinationAddress();
        this.recipientName = entity.getRecipientName();
        this.estimatedArrivalTime = entity.getEstimatedArrivalTime();
        this.actualDeliveryTime = entity.getActualDeliveryTime();
        this.companyDriverId = entity.getCompanyDriverId();

        // 자식 엔티티 리스트도 DTO 리스트로 변환
        this.routeHistories = entity.getRouteHistories().stream()
                .map(RouteHistoryResponse::new) // route -> new RouteHistoryResponse(route)
                .collect(Collectors.toList());
    }
}
