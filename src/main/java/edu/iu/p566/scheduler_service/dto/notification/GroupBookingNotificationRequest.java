package edu.iu.p566.scheduler_service.dto.notification;

import lombok.Data;

@Data
public class GroupBookingNotificationRequest {
    private Long groupId;
    private Long groupAppointmentId;

    private String appointmentDate;
    private String startTime;
    private String endTime;
}
