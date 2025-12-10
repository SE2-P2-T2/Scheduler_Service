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
    private Long bookingId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "individual_appointment_id", nullable = true)
    private Long appointmentId;

    @Column(name = "group_appointment_id", nullable = true)
    private Long groupAppointmentId;

    @Column(name = "group_id", nullable = true)
    private Long groupId;

    @Column(name = "booking_type", nullable = false, length = 20)
    private String bookingType;

    @Column(name = "booked_at", nullable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}