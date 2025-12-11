package edu.iu.p566.scheduler_service.messaging;

import edu.iu.p566.scheduler_service.model.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;


import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final AmqpTemplate amqpTemplate;

    public static final String EXCHANGE = "appointment.exchange";
    public static final String ROUTING_KEY = "booking.created";
    public void publishBookingCreated(BookingCreatedEvent.Payload payload) {
        BookingCreatedEvent event = new BookingCreatedEvent();
        event.setCorrelationId(UUID.randomUUID().toString());
        event.setTimestamp(OffsetDateTime.now());
        event.setPayload(payload);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
                @Override public void beforeCommit(boolean readOnly) {}
                @Override public void beforeCompletion() {}
                @Override public void afterCompletion(int status) {}
                @Override public void suspend() {}
                @Override public void resume() {}
                @Override public void flush() {}
            });
        } else {
            send(event);
        }
    }

    private void send(BookingCreatedEvent event) {
        try {
            log.debug("Publishing booking.created event {}", event.getCorrelationId());
            amqpTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
            log.info("Published booking.created event {}", event.getCorrelationId());
        } catch (Exception ex) {
            log.error("Failed to publish booking.created event", ex);
        }
    }
}


