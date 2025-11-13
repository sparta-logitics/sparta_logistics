package com.example.sparta.hub_service.hub_route.presentation.request;

import java.util.UUID;

public record UpdateHubConnectionRequest(
    UUID departureHubId,
    UUID arrivalHubId,
    Double distanceKm,
    Integer estimatedMinutes
) {}
