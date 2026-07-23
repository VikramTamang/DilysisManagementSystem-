package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyReassignmentResponse {
    private Long staffId;
    private String staffName;
    private LocalDateTime unavailableStart;
    private LocalDateTime unavailableEnd;
    private String reason;
    private int totalAffected;
    private int reassignedCount;
    private int pendingCount;
    private List<ReassignmentResultResponse> results;
}
