package com.sparta.deliveryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "delivery.exchange";
    public static final String QUEUE_NAME = "delivery.completed.queue";
    public static final String ROUTING_KEY = "delivery.completed.key";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue deliveryQueue() {
        return new Queue(QUEUE_NAME, true); // durable 큐
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding deliveryBinding(Queue deliveryQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(deliveryQueue).to(deliveryExchange).with(ROUTING_KEY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
}
