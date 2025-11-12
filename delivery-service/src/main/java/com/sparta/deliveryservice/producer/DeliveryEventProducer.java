package com.sparta.deliveryservice.producer;

import com.sparta.deliveryservice.producer.dto.DeliveryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventProducer {

    // Kafka 의존성이 추가되면 이 부분의 주석을 해제합니다
    // private final KafkaTemplate<String, DeliveryCompletedEvent> kafkaTemplate;
    // private static final String TOPIC = "delivery-complete-topic";

    /**
     * [TDD] (뼈대) 배송 완료 이벤트를 발행합니다.
     */
    public void sendDeliveryCompletedEvent(DeliveryCompletedEvent event) {
        log.info("Kafka Event Published: orderId={}", event.getOrderId());
        // kafkaTemplate.send(TOPIC, event);
    }
}
