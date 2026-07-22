package com.fonepay.gateway.appointment.service.appointment;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.appointment.service.report.AppointmentAuditLogService;
import com.fonepay.gateway.dto.request.RescheduleAppointmentRequest;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.entity.enums.MachineStatus;
import com.fonepay.gateway.entity.enums.RoomStatus;
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
public class RescheduleAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final StaffReportRepository staffReportRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;
    private final AppointmentAuditLogService appointmentAuditLogService;

    @Transactional("appointmentTransactionManager")
    public AppointmentResponse reschedule(Long id, RescheduleAppointmentRequest request,
                                          Long performedByUserId, String performedByRole) {
        log.info("Rescheduling appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException("Cannot reschedule a cancelled appointment", HttpStatus.BAD_REQUEST, "APPOINTMENT_CANCELLED");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException("Cannot reschedule a completed appointment", HttpStatus.BAD_REQUEST, "APPOINTMENT_COMPLETED");
        }

        if (request.getScheduledStart().isAfter(request.getScheduledEnd()) || request.getScheduledStart().isEqual(request.getScheduledEnd())) {
            throw new AppException("Scheduled start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
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

        PatientIdentity patient = patientIdentityRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new AppException("Patient not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND"));

        StaffReport staff = staffReportRepository.findById(appointment.getStaffId())
                .orElseThrow(() -> new AppException("Staff member not found", HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND"));

        // Staff availability at the new time
        if (appointmentRepository.isStaffBooked(staff.getStaffId(), request.getScheduledStart(), request.getScheduledEnd(), appointment.getId())) {
            throw new AppException("Staff is already booked during this time", HttpStatus.BAD_REQUEST, "STAFF_ALREADY_BOOKED");
        }

        // Resolve room: keep the current one unless a new one is requested
        Long targetRoomId = request.getRoomId() != null ? request.getRoomId() : appointment.getRoomId();
        Room room = roomRepository.findById(targetRoomId)
                .orElseThrow(() -> new AppException("Room not found", HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new AppException("Room is under maintenance", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_BOOKED");
        }
        if (appointmentRepository.isRoomBooked(room.getId(), request.getScheduledStart(), request.getScheduledEnd(), appointment.getId())) {
            throw new AppException("Room is already booked during this time", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_BOOKED");
        }

        // Resolve machine: keep the current one unless a new one is requested
        Long targetMachineId = request.getMachineId() != null ? request.getMachineId() : appointment.getMachineId();
        DialysisMachine machine = dialysisMachineRepository.findById(targetMachineId)
                .orElseThrow(() -> new AppException("Dialysis machine not found", HttpStatus.NOT_FOUND, "MACHINE_NOT_FOUND"));
        if (machine.getStatus() != MachineStatus.AVAILABLE) {
            throw new AppException("Dialysis machine is under maintenance", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_BOOKED");
        }
        if (appointmentRepository.isMachineBooked(machine.getId(), request.getScheduledStart(), request.getScheduledEnd(), appointment.getId())) {
            throw new AppException("Dialysis machine is already booked during this time", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_BOOKED");
        }

        appointment.setScheduledStart(request.getScheduledStart());
        appointment.setScheduledEnd(request.getScheduledEnd());
        appointment.setRoomId(room.getId());
        appointment.setMachineId(machine.getId());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment updated = appointmentRepository.save(appointment);

        appointmentAuditLogService.logUpdatedOrRescheduled(before, updated, performedByUserId, performedByRole);

        return CreateAppointmentService.mapToResponse(updated, patient.getName(), staff.getName(), room.getRoomNumber(), machine.getSerialNumber());
    }
}