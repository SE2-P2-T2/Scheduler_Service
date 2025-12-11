package edu.iu.p566.scheduler_service.messaging;

import edu.iu.p566.scheduler_service.model.BookingCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public BookingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookingCreated(BookingCreatedEvent.Payload payload) {
        BookingCreatedEvent evt = new BookingCreatedEvent();
        evt.correlationId = UUID.randomUUID().toString();
        evt.timestamp = OffsetDateTime.now();
        evt.payload = payload;
        rabbitTemplate.convertAndSend("appointment.exchange", "booking.created", evt);
    }
}

