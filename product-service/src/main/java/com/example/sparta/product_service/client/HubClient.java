package com.example.sparta.product_service.client;

import com.example.sparta.hub_service.hub.presentation.response.HubDetailResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Hub 서비스와의 통신을 위한 FeignClient
 * 
 * Hub 서비스의 기존 API를 호출하여 허브 정보를 조회합니다.
 * Hub 서비스의 HubDetailResponse를 그대로 사용합니다.
 */
@FeignClient(name = "hub-service", path = "/hubs")
public interface HubClient {
    
    /**
     * 특정 허브 정보를 조회합니다.
     * 
     * Hub Service의 기존 API: GET /hubs/{hubId}
     * 
     * @param hubId 조회할 허브 ID
     * @return 허브 상세 정보 (Hub Service의 HubDetailResponse)
     * @throws feign.FeignException.NotFound 허브가 존재하지 않는 경우
     */
    @GetMapping("/{hubId}")
    HubDetailResponse getHub(@PathVariable("hubId") UUID hubId);
}