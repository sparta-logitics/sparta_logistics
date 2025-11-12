package com.example.sparta.hub_service.hub_routes.dto;

import java.util.UUID;

public record UpdateHubConnectionCommand(
    UUID departureHubId,
    UUID arrivalHubId,
    Double distanceKm,
    Integer estimatedMinutes
) {
    public static UpdateHubConnectionCommand from(UpdateHubConnectionRequest request) {
        return new UpdateHubConnectionCommand(
            request.departureHubId(),
            request.arrivalHubId(),
            request.distanceKm(),
            request.estimatedMinutes()
        );
    }
}
