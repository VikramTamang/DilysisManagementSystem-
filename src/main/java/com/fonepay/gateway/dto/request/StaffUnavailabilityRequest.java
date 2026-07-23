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
public class StaffUnavailabilityRequest {

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotNull(message = "Unavailable start time is required")
    private LocalDateTime unavailableStart;

    @NotNull(message = "Unavailable end time is required")
    private LocalDateTime unavailableEnd;

    private String reason;
}
