package com.fonepay.gateway.appointment.service.report;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.AppointmentAuditLog;
import com.fonepay.gateway.appointment.repository.AppointmentAuditLogRepository;
import com.fonepay.gateway.entity.enums.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentAuditLogService {

    private final AppointmentAuditLogRepository appointmentAuditLogRepository;

    @Transactional("appointmentTransactionManager")
    public void logCreated(Appointment appointment, Long performedByUserId, String performedByRole) {
        AppointmentAuditLog entry = AppointmentAuditLog.builder()
                .appointmentId(appointment.getId())
                .action(AuditAction.CREATED)
                .performedByUserId(performedByUserId)
                .performedByRole(performedByRole)
                .newStatus(appointment.getStatus())
                .newScheduledStart(appointment.getScheduledStart())
                .newScheduledEnd(appointment.getScheduledEnd())
                .newRoomId(appointment.getRoomId())
                .newMachineId(appointment.getMachineId())
                .build();

        save(entry);
    }

    @Transactional("appointmentTransactionManager")
    public void logCancelled(Appointment before, Long performedByUserId, String performedByRole) {
        AppointmentAuditLog entry = AppointmentAuditLog.builder()
                .appointmentId(before.getId())
                .action(AuditAction.CANCELLED)
                .performedByUserId(performedByUserId)
                .performedByRole(performedByRole)
                .oldStatus(before.getStatus())
                .newStatus(com.fonepay.gateway.entity.enums.AppointmentStatus.CANCELLED)
                .oldScheduledStart(before.getScheduledStart())
                .oldScheduledEnd(before.getScheduledEnd())
                .oldRoomId(before.getRoomId())
                .oldMachineId(before.getMachineId())
                .build();

        save(entry);
    }

    /**
     * Logs an UPDATE or RESCHEDULE. Pass a snapshot of the appointment
     * BEFORE mutation, and the same (now-mutated) appointment AFTER.
     * Action is inferred: if the schedule/room/machine changed, it's a
     * RESCHEDULE; otherwise it's a plain UPDATE (e.g. patient correction).
     */
    @Transactional("appointmentTransactionManager")
    public void logUpdatedOrRescheduled(Appointment before, Appointment after,
                                        Long performedByUserId, String performedByRole) {
        boolean scheduleOrResourceChanged =
                !before.getScheduledStart().equals(after.getScheduledStart())
                        || !before.getScheduledEnd().equals(after.getScheduledEnd())
                        || !before.getRoomId().equals(after.getRoomId())
                        || !before.getMachineId().equals(after.getMachineId());

        AuditAction action = scheduleOrResourceChanged ? AuditAction.RESCHEDULED : AuditAction.UPDATED;

        AppointmentAuditLog entry = AppointmentAuditLog.builder()
                .appointmentId(after.getId())
                .action(action)
                .performedByUserId(performedByUserId)
                .performedByRole(performedByRole)
                .oldStatus(before.getStatus())
                .newStatus(after.getStatus())
                .oldScheduledStart(before.getScheduledStart())
                .newScheduledStart(after.getScheduledStart())
                .oldScheduledEnd(before.getScheduledEnd())
                .newScheduledEnd(after.getScheduledEnd())
                .oldRoomId(before.getRoomId())
                .newRoomId(after.getRoomId())
                .oldMachineId(before.getMachineId())
                .newMachineId(after.getMachineId())
                .build();

        save(entry);
    }

    private void save(AppointmentAuditLog entry) {
        log.info("Audit log: appointmentId={}, action={}, performedBy={}({})",
                entry.getAppointmentId(), entry.getAction(), entry.getPerformedByUserId(), entry.getPerformedByRole());
        appointmentAuditLogRepository.save(entry);
    }
}