package com.sparta.deliveryservice.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteCompleteRequest {

    private UUID driverId;
    private Double actualDistance;
    private Integer actualDuration;

    public RouteCompleteRequest(double v, int i) {
    }
}
