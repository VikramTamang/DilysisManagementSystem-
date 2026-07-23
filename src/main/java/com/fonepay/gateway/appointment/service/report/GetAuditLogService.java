package com.fonepay.gateway.appointment.service.report;

import com.fonepay.gateway.appointment.entity.AppointmentAuditLog;
import com.fonepay.gateway.appointment.repository.AppointmentAuditLogRepository;
import com.fonepay.gateway.dto.response.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAuditLogService {

    private final AppointmentAuditLogRepository appointmentAuditLogRepository;

    public List<AuditLogResponse> getHistoryForAppointment(Long appointmentId) {
        return appointmentAuditLogRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getAllLogs() {
        return appointmentAuditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private AuditLogResponse map(AppointmentAuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .appointmentId(log.getAppointmentId())
                .action(log.getAction())
                .performedByUserId(log.getPerformedByUserId())
                .performedByRole(log.getPerformedByRole())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .oldScheduledStart(log.getOldScheduledStart())
                .newScheduledStart(log.getNewScheduledStart())
                .oldScheduledEnd(log.getOldScheduledEnd())
                .newScheduledEnd(log.getNewScheduledEnd())
                .oldRoomId(log.getOldRoomId())
                .newRoomId(log.getNewRoomId())
                .oldMachineId(log.getOldMachineId())
                .newMachineId(log.getNewMachineId())
                .oldStaffId(log.getOldStaffId())
                .newStaffId(log.getNewStaffId())
                .createdAt(log.getCreatedAt())
                .build();
    }
}