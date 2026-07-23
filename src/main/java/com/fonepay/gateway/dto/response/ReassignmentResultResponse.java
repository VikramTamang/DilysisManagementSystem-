package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignmentResultResponse {
    private Long appointmentId;
    private String outcome;
    private Long previousStaffId;
    private String previousStaffName;
    private Long newStaffId;
    private String newStaffName;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private String note;
}
