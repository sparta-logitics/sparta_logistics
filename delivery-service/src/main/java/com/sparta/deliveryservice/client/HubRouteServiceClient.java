package com.sparta.deliveryservice.client;

import com.sparta.deliveryservice.client.dto.RouteInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub-service", url = "${HUB-SERVICE_RIBBON_LISTOFSERVERS}") // (B. 허브/경로 서비스의 유레카 이름)
public interface HubRouteServiceClient {
    // (테스트의 'getRoutes(UUID, UUID)' 호출을 만족시키는 메서드
    @GetMapping("/internal/routes")
    List<RouteInfoResponse> getRoutes(@RequestParam("originHubId") UUID originHubId,
                                      @RequestParam("destinationHubId") UUID destinationHubId);
}
