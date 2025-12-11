package edu.iu.p566.scheduler_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    public String eventType = "booking.created";
    public String version = "1.0";
    public String correlationId;
    public OffsetDateTime timestamp;
    public Payload payload;

    public static class Payload {
        public Long bookingId;
        public Long studentId;
        public Long appointmentId;
        public Long groupAppointmentId;
        public Long groupId;
        public String bookingType;
        public String status;
        public String notes;
    }
}