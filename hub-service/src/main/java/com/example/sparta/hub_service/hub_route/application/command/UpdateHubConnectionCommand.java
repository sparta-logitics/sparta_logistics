package com.example.sparta.hub_service.hub_route.application.command;

import com.example.sparta.hub_service.hub_route.presentation.request.UpdateHubConnectionRequest;
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
