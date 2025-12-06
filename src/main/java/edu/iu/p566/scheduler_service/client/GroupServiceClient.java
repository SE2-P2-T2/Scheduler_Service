package edu.iu.p566.scheduler_service.client;

import edu.iu.p566.scheduler_service.dto.GroupAppointmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "group-service", url = "${group.service.url:http://localhost:8080}")
public interface GroupServiceClient {

    @GetMapping("/api/groups/{id}")
    GroupAppointmentDTO getGroupById(@PathVariable("id") Long id);
}