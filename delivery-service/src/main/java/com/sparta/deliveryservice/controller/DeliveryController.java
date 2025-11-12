package com.sparta.deliveryservice.controller;

import com.sparta.deliveryservice.controller.dto.request.CompanyDriverAssignRequest;
import com.sparta.deliveryservice.domain.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/delivery/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Flow 1: 배송 생성 (Saga 트랜잭션의 시작)
     * API: POST /api/v1/deliveries (API 명세서와 일치시키기 위해 /internal 경로는 제외)
     */
    @PostMapping
    public ResponseEntity<Void> createDelivery(@RequestBody DeliveryCreateRequest request) {

        // (TDD 리팩토링: createDelivery가 생성된 ID를 반환하도록 수정하는 것이 좋다)
        deliveryService.createDelivery(request);

        // (임시로 생성 성공 201 Created 반환)
        // (UUID를 반환받는다면: URI location = URI.create("/api/v1/deliveries/" + newDeliveryId);)
        return ResponseEntity.created(null).build();
    }

    /**
     * Flow 3-1: 최종 담당자 배정
     * API: PUT /api/v1/deliveries/{deliveryId}/assign-company-driver
     */
    @PutMapping("/{deliveryId}/assign-company-driver")
    public ResponseEntity<Void> assignCompanyDriver(
            @PathVariable UUID deliveryId,
            @RequestBody CompanyDriverAssignRequest request) {

        deliveryService.assignCompanyDriver(deliveryId, request.getCompanyDriverId());

        return ResponseEntity.ok().build();
    }

    /**
     * Flow 3-2: 최종 배송 시작
     * API: PUT /api/v1/deliveries/{deliveryId}/start-company-delivery
     */
    @PutMapping("/{deliveryId}/start-company-delivery")
    public ResponseEntity<Void> startCompanyDelivery(@PathVariable UUID deliveryId) {

        deliveryService.startCompanyDelivery(deliveryId);

        return ResponseEntity.ok().build();
    }

    /**
     * Flow 3-3: 최종 배송 완료 (이벤트 발행)
     * API: PUT /api/v1/deliveries/{deliveryId}/complete-delivery
     */
    @PutMapping("/{deliveryId}/complete-delivery")
    public ResponseEntity<Void> completeDelivery(@PathVariable UUID deliveryId) {

        deliveryService.completeDelivery(deliveryId);

        return ResponseEntity.ok().build();
    }

    // 이 외에 GET /deliveries/{id}, GET /deliveries, DELETE /deliveries/{id} 등
    // API 명세서의 나머지 CRUD API도 여기에 구현한다.
}
