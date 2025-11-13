package com.example.sparta.hub_service.hub.presentation.request;

import java.math.BigDecimal;

public record CreateHubRequest(
    String code,
    String name,
    String address,
    BigDecimal latitude,
    BigDecimal longitude
) {}
