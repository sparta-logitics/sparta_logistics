package com.example.sparta.hub_service.hub_route.presentation.response;

import com.example.sparta.hub_service.hub_route.domain.entity.HubConnection;
import com.example.sparta.hub_service.hub_route.domain.entity.HubRouteSegment;
import java.io.Serializable;
import java.util.UUID;

public record SegmentResponse(
    int sequence,
    UUID hubConnectionId,
    UUID departureHubId,
    UUID arrivalHubId,
    double distanceKm,
    int durationMinutes
) implements Serializable {
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
