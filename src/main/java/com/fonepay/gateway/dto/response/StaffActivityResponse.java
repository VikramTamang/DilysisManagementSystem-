package com.fonepay.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffActivityResponse {
    private Long staffId;
    private String name;
    private String role;
    private long totalAppointments;
    private long scheduledCount;
    private long rescheduledCount;
    private long cancelledCount;
    private long completedCount;
}