package com.example.sparta.hub_service.hub_route.application.command;

import com.example.sparta.hub_service.hub_route.presentation.request.HubConnectionRequest;
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
