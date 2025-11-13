package com.example.sparta.hub_service.hub_route.application.dto;

import com.example.sparta.hub_service.hub_route.domain.entity.HubRoute;
import com.example.sparta.hub_service.hub_route.presentation.response.SegmentResponse;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record HubRouteResult(
    UUID routeId,
    UUID departureHubId,
    UUID arrivalHubId,
    double totalDistanceKm,
    int totalDurationMinutes,
    List<SegmentResponse> segments
) implements Serializable {
    public static HubRouteResult from(HubRoute hubRoute) {
        List<SegmentResponse> segmentResponses = hubRoute
            .getHubRouteSegments()
            .stream()
            .map(segment ->
                SegmentResponse.of(segment, segment.getHubConnection())
            )
            .toList();

        return new HubRouteResult(
            hubRoute.getId(),
            hubRoute.getDepartureHubId().getId(),
            hubRoute.getArrivalHubId().getId(),
            hubRoute.getTotalDistanceKm().getDistance(),
            hubRoute.getTotalEstimatedMinutes().getDuration(),
            segmentResponses
        );
    }
}
