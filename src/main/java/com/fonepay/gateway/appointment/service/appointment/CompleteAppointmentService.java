package com.fonepay.gateway.appointment.service.appointment;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.PatientOperational;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.appointment.service.notification.NotificationService;
import com.fonepay.gateway.appointment.service.report.AppointmentAuditLogService;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;
    private final StaffReportRepository staffReportRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;
    private final AppointmentAuditLogService appointmentAuditLogService;
    private final NotificationService notificationService;

    @Transactional("appointmentTransactionManager")
    public AppointmentResponse completeAppointment(Long id, Long performedByUserId, String performedByRole) {
        log.info("Completing dialysis session for appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("Cannot complete a cancelled appointment", HttpStatus.BAD_REQUEST, "APPOINTMENT_CANCELLED");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException("Appointment is already marked as completed", HttpStatus.BAD_REQUEST, "APPOINTMENT_ALREADY_COMPLETED");
        }

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

        // 1. Mark Appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        // 2. Increment Patient's total completed sessions
        try {
            patientOperationalRepository.findById(appointment.getPatientId()).ifPresent(operational -> {
                int currentSessions = operational.getTotalSessions() != null ? operational.getTotalSessions() : 0;
                operational.setTotalSessions(currentSessions + 1);
                patientOperationalRepository.save(operational);
            });
        } catch (Exception e) {
            log.warn("Could not update patient total sessions: {}", e.getMessage());
        }

        // 3. Log Audit & Notifications
        appointmentAuditLogService.logCompleted(before, performedByUserId, performedByRole);

        // 4. Resolve labels for response
        String patientName = patientIdentityRepository.findById(appointment.getPatientId())
                .map(PatientIdentity::getName).orElse("Patient #" + appointment.getPatientId());
        String staffName = staffReportRepository.findById(appointment.getStaffId())
                .map(StaffReport::getName).orElse("Staff #" + appointment.getStaffId());
        String roomNumber = roomRepository.findById(appointment.getRoomId())
                .map(Room::getRoomNumber).orElse("Room #" + appointment.getRoomId());
        String serialNumber = dialysisMachineRepository.findById(appointment.getMachineId())
                .map(DialysisMachine::getSerialNumber).orElse("Machine #" + appointment.getMachineId());

        return CreateAppointmentService.mapToResponse(saved, patientName, staffName, roomNumber, serialNumber);
    }
}
