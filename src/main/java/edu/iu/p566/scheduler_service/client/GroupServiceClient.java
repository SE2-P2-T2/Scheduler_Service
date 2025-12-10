package edu.iu.p566.scheduler_service.client;

import edu.iu.p566.scheduler_service.dto.GroupAppointmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "group-service", url = "${group.service.url:http://localhost:8080}")
public interface GroupServiceClient {

    @GetMapping("/api/groups/{id}")
    GroupAppointmentDTO getGroupById(@PathVariable("id") Long id);

    @PutMapping("/api/groups/{id}/mark-booked")
    void markGroupAsBooked(
            @PathVariable("id") Long id,
            @RequestParam("bookingId") Long bookingId,
            @RequestParam("userId") Long userId
    );

    @PutMapping("/api/groups/{id}/unmark-booked")
    void unmarkGroupAsBooked(@PathVariable("id") Long id);
}