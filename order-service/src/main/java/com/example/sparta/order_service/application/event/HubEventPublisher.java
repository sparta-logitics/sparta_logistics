package com.example.sparta.order_service.application.event;

import com.example.sparta.order_service.domain.client.HubApiClient;
import com.example.sparta.order_service.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubEventPublisher {
    private final OrderRepository orderRepository;
    private final HubApiClient hubApiClient;
}
