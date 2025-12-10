package edu.iu.p566.scheduler_service.client;

import edu.iu.p566.scheduler_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8083}")
public interface UserServiceClient {

    @GetMapping("/api/users/getusers")
    List<UserDTO> getAllUsers();

    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
