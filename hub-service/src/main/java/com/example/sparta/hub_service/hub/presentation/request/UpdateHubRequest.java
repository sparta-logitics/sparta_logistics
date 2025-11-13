package com.example.sparta.hub_service.hub.presentation.request;

import java.math.BigDecimal;

public record UpdateHubRequest(
    String code,
    String name,
    String address,
    String status,
    BigDecimal latitude,
    BigDecimal longitude
) {}
