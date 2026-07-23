package com.fonepay.gateway.appointment.service.emergency;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.Schedule;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.ScheduleRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.appointment.service.report.AppointmentAuditLogService;
import com.fonepay.gateway.dto.request.StaffUnavailabilityRequest;
import com.fonepay.gateway.dto.response.EmergencyReassignmentResponse;
import com.fonepay.gateway.dto.response.ReassignmentResultResponse;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyReassignmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final StaffReportRepository staffReportRepository;
    private final AppointmentAuditLogService appointmentAuditLogService;

    @Transactional("appointmentTransactionManager")
    public EmergencyReassignmentResponse handleStaffUnavailability(
            StaffUnavailabilityRequest request,
            Long performedByUserId,
            String performedByRole
    ) {
        log.info("Handling emergency unavailability for staffId: {} in window {} to {}",
                request.getStaffId(), request.getUnavailableStart(), request.getUnavailableEnd());

        if (request.getUnavailableStart().isAfter(request.getUnavailableEnd()) ||
                request.getUnavailableStart().equals(request.getUnavailableEnd())) {
            throw new AppException("Unavailable start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        StaffReport unavailableStaff = staffReportRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException("Staff member not found", HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND"));

        List<Appointment> affectedAppointments = appointmentRepository.findActiveAppointmentsForStaffInWindow(
                request.getStaffId(), request.getUnavailableStart(), request.getUnavailableEnd());

        List<ReassignmentResultResponse> results = new ArrayList<>();
        int reassignedCount = 0;
        int pendingCount = 0;

        if (affectedAppointments.isEmpty()) {
            return EmergencyReassignmentResponse.builder()
                    .staffId(request.getStaffId())
                    .staffName(unavailableStaff.getName())
                    .unavailableStart(request.getUnavailableStart())
                    .unavailableEnd(request.getUnavailableEnd())
                    .reason(request.getReason())
                    .totalAffected(0)
                    .reassignedCount(0)
                    .pendingCount(0)
                    .results(results)
                    .build();
        }

        for (Appointment appt : affectedAppointments) {
            String dayOfWeek = appt.getScheduledStart().getDayOfWeek().name();
            List<Schedule> candidates = scheduleRepository.findByDayOfWeekAndIsAvailableTrue(dayOfWeek);

            LocalTime apptStart = appt.getScheduledStart().toLocalTime();
            LocalTime apptEnd = appt.getScheduledEnd().toLocalTime();

            Long assignedStaffId = null;
            String assignedStaffName = null;

            for (Schedule schedule : candidates) {
                if (schedule.getStaffId().equals(request.getStaffId())) {
                    continue; // Skip the unavailable staff member
                }

                // Check if candidate shift covers the appointment slot
                LocalTime shiftStart = schedule.getStartTime();
                LocalTime shiftEnd = schedule.getEndTime();
                boolean coversStart = !shiftStart.isAfter(apptStart);
                boolean coversEnd = !shiftEnd.isBefore(apptEnd);

                if (coversStart && coversEnd) {
                    // Check if candidate is booked during this time
                    boolean isBooked = appointmentRepository.isStaffBooked(
                            schedule.getStaffId(),
                            appt.getScheduledStart(),
                            appt.getScheduledEnd(),
                            appt.getId()
                    );

                    if (!isBooked) {
                        assignedStaffId = schedule.getStaffId();
                        StaffReport candidateStaff = staffReportRepository.findById(assignedStaffId).orElse(null);
                        assignedStaffName = candidateStaff != null ? candidateStaff.getName() : "Unknown Staff";
                        break; // Found an available candidate!
                    }
                }
            }

            // Snapshot the appointment before modification
            Appointment before = Appointment.builder()
                    .id(appt.getId())
                    .patientId(appt.getPatientId())
                    .staffId(appt.getStaffId())
                    .roomId(appt.getRoomId())
                    .machineId(appt.getMachineId())
                    .scheduledStart(appt.getScheduledStart())
                    .scheduledEnd(appt.getScheduledEnd())
                    .status(appt.getStatus())
                    .createdAt(appt.getCreatedAt())
                    .updatedAt(appt.getUpdatedAt())
                    .build();

            if (assignedStaffId != null) {
                // Success: reassign appointment
                appt.setStaffId(assignedStaffId);
                appt.setStatus(AppointmentStatus.RESCHEDULED);
                Appointment updated = appointmentRepository.save(appt);

                appointmentAuditLogService.logReassigned(before, updated, performedByUserId, performedByRole);
                reassignedCount++;

                results.add(ReassignmentResultResponse.builder()
                        .appointmentId(appt.getId())
                        .outcome("REASSIGNED")
                        .previousStaffId(request.getStaffId())
                        .previousStaffName(unavailableStaff.getName())
                        .newStaffId(assignedStaffId)
                        .newStaffName(assignedStaffName)
                        .scheduledStart(appt.getScheduledStart())
                        .scheduledEnd(appt.getScheduledEnd())
                        .note("Successfully reassigned to " + assignedStaffName)
                        .build());
            } else {
                // Failure: mark as pending manual reassignment
                appt.setStatus(AppointmentStatus.PENDING_REASSIGNMENT);
                Appointment updated = appointmentRepository.save(appt);

                appointmentAuditLogService.logPendingReassignment(updated, performedByUserId, performedByRole);
                pendingCount++;

                results.add(ReassignmentResultResponse.builder()
                        .appointmentId(appt.getId())
                        .outcome("PENDING_REASSIGNMENT")
                        .previousStaffId(request.getStaffId())
                        .previousStaffName(unavailableStaff.getName())
                        .newStaffId(null)
                        .newStaffName(null)
                        .scheduledStart(appt.getScheduledStart())
                        .scheduledEnd(appt.getScheduledEnd())
                        .note("No available staff matches this slot")
                        .build());
            }
        }

        return EmergencyReassignmentResponse.builder()
                .staffId(request.getStaffId())
                .staffName(unavailableStaff.getName())
                .unavailableStart(request.getUnavailableStart())
                .unavailableEnd(request.getUnavailableEnd())
                .reason(request.getReason())
                .totalAffected(affectedAppointments.size())
                .reassignedCount(reassignedCount)
                .pendingCount(pendingCount)
                .results(results)
                .build();
    }
}
