package edu.iu.p566.scheduler_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAppointmentDTO {
    private Long groupId;
    private Integer instructorId;
    private String groupName;
    private String startTime;
    private String endTime;
    private Integer maxLimit;
    private String description;
    private String status;
    private String createdAt;
    private Boolean isBooked;
    private Long bookedAppointmentId;
    private Long bookedByUserId;
    private String bookedAt;
}