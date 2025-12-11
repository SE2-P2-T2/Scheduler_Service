package edu.iu.p566.scheduler_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import edu.iu.p566.scheduler_service.dto.notification.IndividualBookingNotificationRequest;

@FeignClient(name = "appointment-service", url = "${appointment.service.url:http://localhost:8084}")
public interface AppointmentServiceClient {

    @GetMapping("/api/appointments/individual/{id}")
    IndividualBookingNotificationRequest getIndividualAppointment(@PathVariable("id") Long id);

}
