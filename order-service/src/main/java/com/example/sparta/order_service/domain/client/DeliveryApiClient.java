package com.example.sparta.order_service.domain.client;

import com.example.sparta.order_service.application.dto.request.DeliveryCreateRequest;
import com.example.sparta.order_service.application.dto.response.DeliveryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service", url = "http://localhost:9005")
public interface DeliveryApiClient {
    @PostMapping("/internal/deliveries")
    DeliveryResponse createDeliveryInfo(@RequestBody DeliveryCreateRequest request);
}
