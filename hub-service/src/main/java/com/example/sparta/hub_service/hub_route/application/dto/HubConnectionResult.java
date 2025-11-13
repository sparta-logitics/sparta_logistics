package com.example.sparta.hub_service.hub_route.application.dto;

import com.example.sparta.hub_service.hub_route.domain.entity.HubConnection;
import java.io.Serializable;
import java.util.UUID;

public record HubConnectionResult(
    UUID hubConnectionId,
    UUID departureHubId,
    UUID arrivalHubId,
    Double distanceKm,
    Integer estimatedMinutes
) implements Serializable {
    public static HubConnectionResult from(HubConnection HubConnection) {
        return new HubConnectionResult(
            HubConnection.getId(),
            HubConnection.getDepartureHubId().getId(),
            HubConnection.getArrivalHubId().getId(),
            HubConnection.getDistanceKm().getDistance(),
            HubConnection.getEstimatedMinutes().getDuration()
        );
    }
}
