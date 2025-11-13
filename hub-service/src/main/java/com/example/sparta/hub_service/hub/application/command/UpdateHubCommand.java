package com.example.sparta.hub_service.hub.application.command;

import java.math.BigDecimal;

public record UpdateHubCommand(
    String code,
    String name,
    String address,
    String status,
    BigDecimal latitude,
    BigDecimal longitude
) {}
