package edu.iu.p566.scheduler_service.dto;

import lombok.Data;

@Data
public class GroupBookingRequest {
    private Long studentId;
    private Long groupId;
}
