package com.fonepay.gateway.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dedicated payload for a reschedule action. Deliberately does NOT allow
 * changing patientId/staffId here - swapping the patient or staff on an
 * appointment isn't a "reschedule", it's an edit, and should go through
 * the generic update endpoint instead. Room/machine are optional: if left
 * null, the appointment keeps its currently assigned resource as long as
 * it's still free at the new time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleAppointmentRequest {

    @NotNull(message = "New scheduled start time is required")
    private LocalDateTime scheduledStart;

    @NotNull(message = "New scheduled end time is required")
    private LocalDateTime scheduledEnd;

    private Long roomId;

    private Long machineId;
}