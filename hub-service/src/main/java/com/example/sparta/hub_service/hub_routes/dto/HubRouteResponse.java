package com.example.sparta.hub_service.hub_routes.dto;

import java.util.List;
import java.util.UUID;

public record HubRouteResponse(
    UUID routeId,
    UUID departureHubId,
    UUID arrivalHubId,
    double totalDistanceKm,
    int totalDurationMinutes,
    List<SegmentResponse> segments
) {
    public static HubRouteResponse from(HubRouteResult result) {
        return new HubRouteResponse(
            result.routeId(),
            result.departureHubId(),
            result.arrivalHubId(),
            result.totalDistanceKm(),
            result.totalDurationMinutes(),
            result.segments()
        );
    }
}
