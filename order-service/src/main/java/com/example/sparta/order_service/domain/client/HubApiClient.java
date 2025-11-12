package com.example.sparta.order_service.domain.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "hub-service", url = "http://localhost:9002")
public interface HubApiClient {

}
