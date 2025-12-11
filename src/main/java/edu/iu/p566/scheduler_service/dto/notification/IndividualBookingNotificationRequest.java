package edu.iu.p566.scheduler_service.dto.notification;

import lombok.Data;

@Data
public class IndividualBookingNotificationRequest {
    private Long studentId;
    private Long appointmentId;

    private String appointmentDate;
    private String startTime;
    private String endTime;
}

