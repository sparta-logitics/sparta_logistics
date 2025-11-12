package com.example.sparta.order_service.application.event;

import com.example.sparta.order_service.application.dto.request.DeliveryCreateRequest;
import com.example.sparta.order_service.application.dto.response.DeliveryResponse;
import com.example.sparta.order_service.domain.client.DeliveryApiClient;
import com.example.sparta.order_service.domain.entity.Order;
import com.example.sparta.order_service.domain.entity.OrderStatus;
import com.example.sparta.order_service.domain.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventPublisher {
    private final DeliveryApiClient deliveryApiClient;
    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            value = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void handleOrderCreateEvent(OrderCreateEvent event) {
        DeliveryCreateRequest request = event.toRequest();

        // 임시 객체임
        DeliveryResponse response;
        try {
            response = deliveryApiClient.createDeliveryInfo(request);
        } catch (FeignException e) {
            log.error("Feign call failed for orderId: {}", request.orderId(), e);
            throw e;
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow();
        if(response.isFail())
            order.changeOrderStatus(OrderStatus.FAIL);
        else {
            order.changeOrderStatus(OrderStatus.PREPARING_FOR_SHIPMENT);
            order.assignDeliveryId(response.deliveryId());
        }

        orderRepository.save(order);

        log.info("order create Event success");
    }

    @Recover
    public void recover(FeignException e) {

    }
}
