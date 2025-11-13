package com.example.sparta.hub_service.hub.application.dto;

import com.example.sparta.hub_service.hub.domain.entity.Hub;
import com.example.sparta.hub_service.hub.domain.vo.HubCode;
import com.example.sparta.hub_service.hub.domain.vo.HubStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record HubResult(
    UUID hubId,
    HubCode code,
    String name,
    String address,
    HubStatus status,
    BigDecimal latitude,
    BigDecimal longitude
) implements Serializable {
    public static HubResult from(Hub hub) {
        return new HubResult(
            hub.getId(),
            hub.getCode(),
            hub.getName(),
            hub.getAddress().getAddress(),
            hub.getStatus(),
            hub.getHubLocation().getLatitude(),
            hub.getHubLocation().getLongitude()
        );
    }
}
