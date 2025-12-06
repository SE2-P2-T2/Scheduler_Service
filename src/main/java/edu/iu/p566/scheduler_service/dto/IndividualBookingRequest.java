package edu.iu.p566.scheduler_service.dto;

import lombok.Data;

@Data
public class IndividualBookingRequest {
    private Long studentId;
    private Long appointmentId;
    private String description;
    private String status;
}
