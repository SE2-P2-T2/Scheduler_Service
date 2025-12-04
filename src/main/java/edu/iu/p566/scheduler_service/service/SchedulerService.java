package edu.iu.p566.scheduler_service.service;

import edu.iu.p566.scheduler_service.model.SchedulerAppointment;
import edu.iu.p566.scheduler_service.repository.SchedulerAppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final SchedulerAppointmentRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users/";
    private static final String INDIVIDUAL_APPT_URL = "http://localhost:8083/api/appointments/individual/";
    private static final String GROUP_SERVICE_URL = "http://localhost:8082/api/groups/";

    public SchedulerAppointment bookIndividualAppointment(Long studentId, Long appointmentId) {
        // Validate student & appointment (optional)
        restTemplate.getForObject(USER_SERVICE_URL + studentId, Object.class);
        restTemplate.getForObject(INDIVIDUAL_APPT_URL + appointmentId, Object.class);

        SchedulerAppointment booking = SchedulerAppointment.builder()
                .studentId(studentId)
                .individualAppointmentId(appointmentId)
                .bookingType("individual")
                .bookedAt(OffsetDateTime.now())
                .status("confirmed")
                .build();

        return repository.save(booking);
    }

    public SchedulerAppointment bookGroupAppointment(Long studentId, Long groupId) {
        restTemplate.getForObject(USER_SERVICE_URL + studentId, Object.class);
        restTemplate.getForObject(GROUP_SERVICE_URL + groupId, Object.class);

        SchedulerAppointment booking = SchedulerAppointment.builder()
                .studentId(studentId)
                .groupAppointmentId(groupId)
                .bookingType("group")
                .bookedAt(OffsetDateTime.now())
                .status("confirmed")
                .build();

        return repository.save(booking);
    }

    public void cancelAppointment(Long bookingId, String reason) {
        SchedulerAppointment booking = repository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("cancelled");
        booking.setCancelledAt(OffsetDateTime.now());
        booking.setCancellationReason(reason);
        repository.save(booking);
    }

    public List<SchedulerAppointment> getAppointmentsForStudent(Long studentId) {
        return repository.findByStudentId(studentId);
    }
}
