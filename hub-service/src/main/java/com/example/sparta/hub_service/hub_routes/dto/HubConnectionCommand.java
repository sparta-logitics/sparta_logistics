package com.example.sparta.hub_service.hub_routes.dto;

import java.util.UUID;

public record HubConnectionCommand(
    UUID departureHubId,
    UUID arrivalHubId,
    Double distanceKm,
    Integer estimatedMinutes
) {
    public static HubConnectionCommand from(HubConnectionRequest request) {
        return new HubConnectionCommand(
            request.departureHubId(),
            request.arrivalHubId(),
            request.distanceKm(),
            request.estimatedMinutes()
        );
    }
}
