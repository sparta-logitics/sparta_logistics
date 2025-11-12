package com.sparta.deliveryservice.controller;

import com.sparta.deliveryservice.controller.dto.request.DriverAssignRequest;
import com.sparta.deliveryservice.controller.dto.request.RouteCompleteRequest;
import com.sparta.deliveryservice.service.DeliveryRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/delivery/delivery-routes")
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    /**
     * Flow 2-1: 허브 간 담당자 배정
     * API: PUT /api/v1/delivery-routes/{routeHistoryId}/assign-driver
     */
    @PutMapping("/{routeHistoryId}/assign-driver")
    public ResponseEntity<Void> assignDriver(
            @PathVariable UUID routeHistoryId,
            @RequestBody DriverAssignRequest request) { // Request Body 로 driverId 받기

        deliveryRouteService.assignDriver(routeHistoryId, request.getDriverId());

        // 성공 시 200 OK, 반환값 없음
        return ResponseEntity.ok().build();
    }

    /**
     * Flow 2-2: 배송 시작
     * API: PUT /api/v1/delivery-routes/{routeHistoryId}/start
     */
    @PutMapping("/{routeHistoryId}/start")
    public ResponseEntity<Void> startRoute(@PathVariable UUID routeHistoryId) {

        deliveryRouteService.startRoute(routeHistoryId);

        return ResponseEntity.ok().build();
    }

    /**
     * Flow 2-3: 허브 도착 (완료)
     * API: PUT /api/v1/delivery-routes/{routeHistoryId}/complete
     */
    @PutMapping("/{routeHistoryId}/complete")
    public ResponseEntity<Void> completeRoute(
            @PathVariable UUID routeHistoryId,
            @RequestBody RouteCompleteRequest request) { // Request Body로 실제 기록 받기

        deliveryRouteService.completeRoute(
                routeHistoryId,
                request.getActualDistance(),
                request.getActualDuration()
        );

        return ResponseEntity.ok().build();
    }
}
