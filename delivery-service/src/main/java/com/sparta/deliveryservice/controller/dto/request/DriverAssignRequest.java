package com.sparta.deliveryservice.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignRequest {

    private UUID driverId;
    private Double actualDistance;
    private Integer actualDuration;

    public DriverAssignRequest(UUID driverId) {
        this.driverId = driverId;
    }
}
