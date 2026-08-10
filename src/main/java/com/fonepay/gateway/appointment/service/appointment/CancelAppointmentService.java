package com.fonepay.gateway.appointment.service.appointment;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.service.notification.NotificationService;
import com.fonepay.gateway.appointment.service.report.AppointmentAuditLogService;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentAuditLogService appointmentAuditLogService;
    private final NotificationService notificationService;

    @Transactional("appointmentTransactionManager")
    public void cancelAppointment(Long id) {
        cancelAppointment(id, null, null);
    }

    @Transactional("appointmentTransactionManager")
    public void cancelAppointment(Long id, Long performedByUserId, String performedByRole) {
        log.info("Soft cancelling appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

        Appointment before = Appointment.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .staffId(appointment.getStaffId())
                .roomId(appointment.getRoomId())
                .machineId(appointment.getMachineId())
                .scheduledStart(appointment.getScheduledStart())
                .scheduledEnd(appointment.getScheduledEnd())
                .status(appointment.getStatus())
                .build();

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        appointmentAuditLogService.logCancelled(before, performedByUserId, performedByRole);
        notificationService.notifyAppointmentCancelled(before);
    }
}