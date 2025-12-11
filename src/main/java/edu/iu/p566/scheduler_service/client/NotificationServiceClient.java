package edu.iu.p566.scheduler_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import edu.iu.p566.scheduler_service.dto.notification.CancelNotificationRequest;
import edu.iu.p566.scheduler_service.dto.notification.GroupBookingNotificationRequest;
import edu.iu.p566.scheduler_service.dto.notification.IndividualBookingNotificationRequest;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8086}")
public interface NotificationServiceClient {

    @PostMapping("/notify/individual/booked")
    void sendIndividualBooked(@RequestBody IndividualBookingNotificationRequest request);

    @PostMapping("/notify/individual/cancelled")
    void sendIndividualCancelled(@RequestBody CancelNotificationRequest request);

    @PostMapping("/notify/group/booked")
    void sendGroupBooked(@RequestBody GroupBookingNotificationRequest request);  // ✅ REQUIRED

    @PostMapping("/notify/group/cancelled")
    void sendGroupCancelled(@RequestBody CancelNotificationRequest request);
}


