package edu.iu.p566.scheduler_service.dto.notification;

import lombok.Data;

@Data
public class CancelNotificationRequest {

    private Long bookingId;
    private String bookingType;

    private Long studentId;
    private Long individualAppointmentId;

    private Long groupId;
    private Long groupAppointmentId;

    private String reason;
    private String cancelledAt;
}
