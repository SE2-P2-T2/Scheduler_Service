package edu.iu.p566.scheduler_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "booked_appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "appointment_id")
    private Long individualAppointmentId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "group_id")
    private Long groupAppointmentId;

    @Column(name = "booking_type")
    private String bookingType; // "individual" or "group"

    @Column(name = "booked_at")
    private OffsetDateTime bookedAt;

    @Column(name = "status")
    private String status; // "confirmed", "cancelled"

    @Column(name = "notes")
    private String notes;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;
}
