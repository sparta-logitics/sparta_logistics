package com.example.sparta.hub_service.hub_routes.dto;

import com.example.sparta.hub_service.core.domain.HubConnection;
import com.example.sparta.hub_service.core.domain.HubRouteSegment;
import java.util.UUID;

public record SegmentResponse(
    int sequence,
    UUID hubConnectionId,
    UUID departureHubId,
    UUID arrivalHubId,
    double distanceKm,
    int durationMinutes
) {
    public static SegmentResponse of(
        HubRouteSegment segment,
        HubConnection connection
    ) {
        return new SegmentResponse(
            segment.getSegmentSequence(),
            segment.getHubConnection().getId(),
            connection.getDepartureHubId().getId(),
            connection.getArrivalHubId().getId(),
            connection.getDistanceKm().getDistance(),
            connection.getEstimatedMinutes().getDuration()
        );
    }
}
