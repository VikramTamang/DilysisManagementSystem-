package com.fonepay.gateway.dto.response;

import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.entity.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long appointmentId;
    private AuditAction action;
    private Long performedByUserId;
    private String performedByRole;
    private AppointmentStatus oldStatus;
    private AppointmentStatus newStatus;
    private LocalDateTime oldScheduledStart;
    private LocalDateTime newScheduledStart;
    private LocalDateTime oldScheduledEnd;
    private LocalDateTime newScheduledEnd;
    private Long oldRoomId;
    private Long newRoomId;
    private Long oldMachineId;
    private Long newMachineId;
    private Long oldStaffId;
    private Long newStaffId;
    private LocalDateTime createdAt;
}