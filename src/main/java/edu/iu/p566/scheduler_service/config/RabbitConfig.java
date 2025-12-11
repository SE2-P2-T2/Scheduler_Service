package edu.iu.p566.scheduler_service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "appointment.exchange";
    public static final String ROUTING_BOOKING_CREATED = "booking.created";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Binding bindNotificationQueue(Queue notificationQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(appointmentExchange)
                .with(ROUTING_BOOKING_CREATED);
    }

    // Optional DLX + DLQ
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(EXCHANGE + ".dlx", true, false);
    }
    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE + ".dlq").build();
    }
    @Bean
    public Binding dlqBinding(Queue dlq, TopicExchange dlxExchange) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("#");
    }
}

