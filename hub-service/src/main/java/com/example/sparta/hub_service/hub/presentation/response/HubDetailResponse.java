package com.example.sparta.hub_service.hub.presentation.response;

import com.example.sparta.hub_service.hub.application.dto.HubResult;
import com.example.sparta.hub_service.hub.domain.vo.HubCode;
import com.example.sparta.hub_service.hub.domain.vo.HubStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record HubDetailResponse(
    UUID hubId,
    HubCode code,
    String name,
    String address,
    HubStatus status,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public static HubDetailResponse from(HubResult result) {
        return new HubDetailResponse(
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
