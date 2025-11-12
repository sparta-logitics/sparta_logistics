package com.sparta.deliveryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQDeclarer implements ApplicationRunner {

    private final RabbitAdmin rabbitAdmin;
    private final Queue deliveryQueue;
    private final TopicExchange deliveryExchange;
    private final Binding deliveryBinding;

    // ✨ 이제 생성자 주입은 순환 참조를 일으키지 않습니다.
    // RabbitMQConfig 빈이 아닌, 이미 생성된 RabbitAdmin 빈 등을 주입받기 때문입니다.
    public RabbitMQDeclarer(RabbitAdmin rabbitAdmin, Queue deliveryQueue,
                            TopicExchange deliveryExchange, Binding deliveryBinding) {
        this.rabbitAdmin = rabbitAdmin;
        this.deliveryQueue = deliveryQueue;
        this.deliveryExchange = deliveryExchange;
        this.deliveryBinding = deliveryBinding;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 컨텍스트가 완전히 뜬 후, 큐 선언 실행
        rabbitAdmin.declareQueue(deliveryQueue);
        rabbitAdmin.declareExchange(deliveryExchange);
        rabbitAdmin.declareBinding(deliveryBinding);
        System.out.println("✅ RabbitMQ components declared successfully (via ApplicationRunner).");
    }
}