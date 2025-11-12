package com.sparta.deliveryservice.dto.response;

import com.sparta.deliveryservice.domain.DeliveryRouteHistory;
import com.sparta.deliveryservice.domain.enums.RouteStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RouteHistoryResponse {

    // 클라이언트에 보여줄 경로 정보
    private UUID routeHistoryId;
    private Integer sequence;
    private UUID originHubId;
    private UUID destinationHubId;
    private RouteStatus status;
    private Double actualDistance;
    private Integer actualDuration;
    private UUID driverId;

    public RouteHistoryResponse(DeliveryRouteHistory entity) {
        this.routeHistoryId = entity.getRouteHistoryId();
        this.sequence = entity.getSequence();
        this.originHubId = entity.getOriginHubId();
        this.destinationHubId = entity.getDestinationHubId();
        this.status = entity.getStatus();
        this.actualDistance = entity.getActualDistance();
        this.actualDuration = entity.getActualDuration();
        this.driverId = entity.getDriverId();
    }
}
