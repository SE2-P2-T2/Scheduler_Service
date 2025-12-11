package edu.iu.p566.scheduler_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private String eventType = "booking.created";
    private String version = "1.0";
    private String correlationId;
    private OffsetDateTime timestamp;
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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