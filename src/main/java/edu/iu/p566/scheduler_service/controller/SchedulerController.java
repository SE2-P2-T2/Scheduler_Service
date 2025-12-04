package edu.iu.p566.scheduler_service.controller;

import edu.iu.p566.scheduler_service.model.SchedulerAppointment;
import edu.iu.p566.scheduler_service.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService service;

    // DTOs for JSON
    @Data
    static class IndividualBookingRequest {
        private Long studentId;
        private Long appointmentId;
    }

    @Data
    static class GroupBookingRequest {
        private Long studentId;
        private Long groupId;
    }

    @Data
    static class CancelBookingRequest {
        private String reason;
    }

    // Book individual appointment
    @PostMapping("/book/individual")
    public ResponseEntity<SchedulerAppointment> bookIndividual(@RequestBody IndividualBookingRequest request) {
        return ResponseEntity.ok(
                service.bookIndividualAppointment(request.getStudentId(), request.getAppointmentId())
        );
    }

    // Book group appointment
    @PostMapping("/book/group")
    public ResponseEntity<SchedulerAppointment> bookGroup(@RequestBody GroupBookingRequest request) {
        return ResponseEntity.ok(
                service.bookGroupAppointment(request.getStudentId(), request.getGroupId())
        );
    }

    // Cancel booking
    @DeleteMapping("/cancel/{bookingId}")
    public ResponseEntity<Void> cancel(@PathVariable Long bookingId, @RequestBody CancelBookingRequest request) {
        service.cancelAppointment(bookingId, request.getReason());
        return ResponseEntity.noContent().build();
    }

    // Get student bookings
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SchedulerAppointment>> getStudentAppointments(@PathVariable Long studentId) {
        return ResponseEntity.ok(service.getAppointmentsForStudent(studentId));
    }
}
