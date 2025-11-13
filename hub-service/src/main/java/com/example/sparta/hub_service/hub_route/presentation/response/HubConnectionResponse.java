package com.example.sparta.hub_service.hub_route.presentation.response;

import com.example.sparta.hub_service.hub_route.application.dto.HubConnectionResult;
import java.util.UUID;

public record HubConnectionResponse(
    UUID id,
    UUID hubConnectionId,
    UUID departureHubId,
    UUID arrivalHubId,
    Double distanceKm,
    Integer estimatedMinutes
) {

    public static HubConnectionResponse from(HubConnectionResult result) {
        return new HubConnectionResponse(
            result.hubConnectionId(),
            result.hubConnectionId(),
            result.departureHubId(),
            result.arrivalHubId(),
            result.distanceKm(),
            result.estimatedMinutes()
        );
    }
}
