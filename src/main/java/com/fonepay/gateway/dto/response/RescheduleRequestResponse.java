package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.RescheduleRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequestResponse {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private LocalDateTime currentScheduledStart;
    private LocalDateTime currentScheduledEnd;
    private LocalDateTime requestedStart;
    private LocalDateTime requestedEnd;
    private String reason;
    private RescheduleRequestStatus status;
    private String reviewNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}