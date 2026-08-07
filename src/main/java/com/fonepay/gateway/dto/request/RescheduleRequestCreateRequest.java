package com.fonepay.gateway.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequestCreateRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Requested start time is required")
    private LocalDateTime requestedStart;

    @NotNull(message = "Requested end time is required")
    private LocalDateTime requestedEnd;

    private String reason;
}