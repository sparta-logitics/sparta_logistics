package com.example.sparta.hub_service.hub.presentation.response;

import java.util.UUID;

public record HubCreateResponse(
    UUID hubId,
    String message
) {}
