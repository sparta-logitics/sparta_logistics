package com.example.sparta.hub_service.hubs.dto;

import com.example.sparta.hub_service.core.enums.HubCode;
import com.example.sparta.hub_service.core.enums.HubStatus;
import com.example.sparta.hub_service.hubs.HubResult;
import java.math.BigDecimal;
import java.util.UUID;

public record HubSearchResponse(
    UUID hubId,
    HubCode code,
    String name,
    String address,
    HubStatus status,
    BigDecimal latitude,
    BigDecimal longitude

) {
    public static HubSearchResponse from(HubResult result) {
        return new HubSearchResponse(
            result.hubId(),
            result.code(),
            result.name(),
            result.address(),
            result.status(),
            result.latitude(),
            result.longitude()
        );
    }
}
