package edu.iu.p566.scheduler_service.controller;

import edu.iu.p566.scheduler_service.dto.GroupMemberDTO;
import edu.iu.p566.scheduler_service.dto.UserDTO;
import edu.iu.p566.scheduler_service.model.SchedulerAppointment;
import edu.iu.p566.scheduler_service.service.SchedulerService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final SchedulerService service;


    @PostMapping("/book/individual")
    public ResponseEntity<SchedulerAppointment> bookIndividual(@RequestBody BookIndividualRequest request) {
        log.info("POST /api/scheduler/book/individual - studentId: {}, appointmentId: {}",
                request.getStudentId(), request.getAppointmentId());
        SchedulerAppointment booking = service.bookIndividualAppointment(
                request.getStudentId(),
                request.getAppointmentId(),
                "confirmed",
                request.getDescription()
        );
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/group/{groupId}/join")
    public ResponseEntity<GroupMemberDTO> joinGroup(
            @PathVariable Long groupId,
            @RequestBody JoinGroupRequest request) {
        log.info("POST /api/scheduler/group/{}/join - studentId: {}", groupId, request.getStudentId());
        GroupMemberDTO member = service.joinGroup(request.getStudentId(), groupId);
        return ResponseEntity.ok(member);
    }

    @PostMapping("/group/{groupId}/book")
    public ResponseEntity<SchedulerAppointment> bookGroupForAll( @RequestBody BookGroupForAllRequest request) {
        log.info("POST /api/scheduler/group/{}/book - studentId: {}, groupAppointmentId: {}", request.getStudentId(), request.getGroupAppointmentId());
        SchedulerAppointment booking = service.bookGroupAppointmentForAll(
                request.getStudentId(),
                request.getGroupId(),
                request.getDescription(),
                request.getGroupAppointmentId()
        );
        return ResponseEntity.ok(booking);
    }

    @DeleteMapping("/group/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            @RequestParam Long studentId) {
        log.info("DELETE /api/scheduler/group/{}/leave - studentId: {}", groupId, studentId);
        service.leaveGroup(studentId, groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/group/{groupId}/is-member")
    public ResponseEntity<Boolean> isUserMemberOfGroup(
            @PathVariable Long groupId,
            @RequestParam Long studentId) {
        log.info("GET /api/scheduler/group/{}/is-member - studentId: {}", groupId, studentId);
        return ResponseEntity.ok(service.isUserMemberOfGroup(studentId, groupId));
    }

    @PutMapping("/cancel/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId, @RequestBody CancelBookingRequest request) {
        log.info("PUT /api/scheduler/cancel/{} - reason: {}", bookingId, request.getReason());
        service.cancelAppointment(bookingId, request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/group/{groupId}/members")
    public ResponseEntity<List<GroupMemberDTO>> getGroupMembers(@PathVariable Long groupId) {
        log.info("GET /api/scheduler/group/{}/members", groupId);
        return ResponseEntity.ok(service.getGroupMembers(groupId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SchedulerAppointment>> getStudentAppointments(@PathVariable Long studentId) {
        log.info("GET /api/scheduler/student/{}", studentId);
        return ResponseEntity.ok(service.getAppointmentsForStudent(studentId));
    }

    @GetMapping("/instructors")
    public ResponseEntity<List<UserDTO>> getAllInstructors() {
        log.info("GET /api/scheduler/instructors");
        return ResponseEntity.ok(service.getAllInstructors());
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<UserDTO> getInstructorById(@PathVariable Long instructorId) {
        log.info("GET /api/scheduler/instructor/{}", instructorId);
        return ResponseEntity.ok(service.getInstructorById(instructorId));
    }

    @GetMapping("/bookings/individual")
    public ResponseEntity<List<SchedulerAppointment>> getIndividualBookings() {
        log.info("GET /api/scheduler/bookings/individual");
        return ResponseEntity.ok(service.getIndividualAppointmentBookings());
    }

    @GetMapping("/bookings/group")
    public ResponseEntity<List<SchedulerAppointment>> getGroupBookings() {
        log.info("GET /api/scheduler/bookings/group");
        return ResponseEntity.ok(service.getGroupAppointmentBookings());
    }

    @GetMapping("/bookings/all")
    public ResponseEntity<List<SchedulerAppointment>> getAllBookings() {
        log.info("GET /api/scheduler/bookings/all");
        return ResponseEntity.ok(service.getAllBookings());
    }

    @GetMapping("/bookings/type/{bookingType}")
    public ResponseEntity<List<SchedulerAppointment>> getBookingsByType(@PathVariable String bookingType) {
        log.info("GET /api/scheduler/bookings/type/{}", bookingType);
        return ResponseEntity.ok(service.getBookingsByType(bookingType));
    }

    @GetMapping("/bookings/status/{status}")
    public ResponseEntity<List<SchedulerAppointment>> getBookingsByStatus(@PathVariable String status) {
        log.info("GET /api/scheduler/bookings/status/{}", status);
        return ResponseEntity.ok(service.getBookingsByStatus(status));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookIndividualRequest {
        private Long studentId;
        private Long appointmentId;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinGroupRequest {
        private Long studentId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookGroupForAllRequest {
        private Long studentId;
        private String description;
        private Long groupAppointmentId;
        private Long groupId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelBookingRequest {
        private String reason;
    }
}