package com.fonepay.gateway.appointment.service.appointment;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.service.notification.NotificationService;
import com.fonepay.gateway.appointment.service.report.AppointmentAuditLogService;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.appointment.repository.ScheduleRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.request.AppointmentRequest;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;
    private final StaffReportRepository staffReportRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentAuditLogService appointmentAuditLogService;
    private final NotificationService notificationService;

    @Transactional("appointmentTransactionManager")
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        return createAppointment(request, null, null);
    }

    @Transactional("appointmentTransactionManager")
    public AppointmentResponse createAppointment(AppointmentRequest request, Long performedByUserId, String performedByRole) {
        log.info("Creating appointment for patientId: {}, staffId: {}", request.getPatientId(), request.getStaffId());

        if (request.getPatientId() == null) {
            throw new AppException("Patient ID is required", HttpStatus.BAD_REQUEST, "PATIENT_ID_REQUIRED");
        }

        if (request.getScheduledStart() == null || request.getScheduledEnd() == null) {
            throw new AppException("Start and end time are required", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        if (request.getScheduledStart().isAfter(request.getScheduledEnd()) || request.getScheduledStart().isEqual(request.getScheduledEnd())) {
            throw new AppException("Scheduled start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        PatientIdentity patient = patientIdentityRepository.findById(request.getPatientId())
                .orElseThrow(() -> new AppException("Patient not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND"));

        // 1. Validate Patient availability (Prevent Double-Booking)
        if (appointmentRepository.isPatientBooked(patient.getId(), request.getScheduledStart(), request.getScheduledEnd(), null)) {
            throw new AppException("Patient already has an active scheduled appointment during this time slot", HttpStatus.BAD_REQUEST, "PATIENT_ALREADY_BOOKED");
        }

        // 2. Resolve & Validate Staff availability (Enforce Doctor Occupancy)
        StaffReport staff;
        if (request.getStaffId() != null) {
            staff = staffReportRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new AppException("Staff member not found", HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND"));

            if (!"ACTIVE".equalsIgnoreCase(staff.getStatus())) {
                throw new AppException("Selected medical staff account is currently inactive", HttpStatus.BAD_REQUEST, "STAFF_INACTIVE");
            }

            if (appointmentRepository.isStaffBooked(staff.getStaffId(), request.getScheduledStart(), request.getScheduledEnd(), null)) {
                throw new AppException("Doctor/Staff is already occupied during this time slot", HttpStatus.BAD_REQUEST, "STAFF_ALREADY_BOOKED");
            }
        } else {
            staff = resolveAvailableStaff(patient.getId(), request.getScheduledStart(), request.getScheduledEnd());
            if (staff == null) {
                throw new AppException("No medical staff available for the requested slot", HttpStatus.BAD_REQUEST, "STAFF_ALREADY_BOOKED");
            }
        }

        // 3. Resolve Room
        Room room;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new AppException("Room not found", HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

            if (room.getStatus() != RoomStatus.AVAILABLE) {
                throw new AppException("Room is under maintenance", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_BOOKED");
            }

            if (appointmentRepository.isRoomBooked(room.getId(), request.getScheduledStart(), request.getScheduledEnd(), null)) {
                throw new AppException("Room is already booked during this time", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_BOOKED");
            }
        } else {
            List<Room> availableRooms = roomRepository.findAvailableRooms(request.getScheduledStart(), request.getScheduledEnd());
            if (availableRooms.isEmpty()) {
                throw new AppException("No available rooms for the requested slot", HttpStatus.BAD_REQUEST, "ROOM_ALREADY_BOOKED");
            }
            room = availableRooms.get(0);
        }

        // 4. Resolve Machine
        DialysisMachine machine;
        if (request.getMachineId() != null) {
            machine = dialysisMachineRepository.findById(request.getMachineId())
                    .orElseThrow(() -> new AppException("Dialysis machine not found", HttpStatus.NOT_FOUND, "MACHINE_NOT_FOUND"));

            if (machine.getStatus() != MachineStatus.AVAILABLE) {
                throw new AppException("Dialysis machine is under maintenance", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_BOOKED");
            }

            if (appointmentRepository.isMachineBooked(machine.getId(), request.getScheduledStart(), request.getScheduledEnd(), null)) {
                throw new AppException("Dialysis machine is already booked during this time", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_BOOKED");
            }
        } else {
            List<DialysisMachine> availableMachines = dialysisMachineRepository.findAvailableMachines(request.getScheduledStart(), request.getScheduledEnd());
            if (availableMachines.isEmpty()) {
                throw new AppException("No available dialysis machines for the requested slot", HttpStatus.BAD_REQUEST, "MACHINE_ALREADY_BOOKED");
            }
            machine = availableMachines.get(0);
        }

        AppointmentStatus initialStatus = Boolean.TRUE.equals(request.getIsEmergency())
                ? AppointmentStatus.EMERGENCY
                : AppointmentStatus.SCHEDULED;

        Appointment appointment = Appointment.builder()
                .patientId(patient.getId())
                .staffId(staff.getStaffId())
                .roomId(room.getId())
                .machineId(machine.getId())
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .status(initialStatus)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        appointmentAuditLogService.logCreated(saved, performedByUserId, performedByRole);
        notificationService.notifyAppointmentConfirmed(saved);

        return mapToResponse(saved, patient.getName(), staff.getName(), room.getRoomNumber(), machine.getSerialNumber());
    }

    private StaffReport resolveAvailableStaff(Long patientId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        // 1. Try patient's assigned doctor first
        try {
            var operational = patientOperationalRepository.findById(patientId).orElse(null);
            if (operational != null && operational.getAssignedDoctorId() != null) {
                Long assignedDocId = operational.getAssignedDoctorId();
                var docOpt = staffReportRepository.findById(assignedDocId);
                if (docOpt.isPresent() && "ACTIVE".equalsIgnoreCase(docOpt.get().getStatus())) {
                    if (!appointmentRepository.isStaffBooked(assignedDocId, start, end, null)) {
                        return docOpt.get();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not check assigned doctor for patientId {}: {}", patientId, e.getMessage());
        }

        // 2. Try staff on duty via schedule
        try {
            String dayOfWeek = start.getDayOfWeek().name();
            var schedules = scheduleRepository.findByDayOfWeekAndIsAvailableTrue(dayOfWeek);
            var apptStart = start.toLocalTime();
            var apptEnd = end.toLocalTime();

            for (var schedule : schedules) {
                if (!schedule.getStartTime().isAfter(apptStart) && !schedule.getEndTime().isBefore(apptEnd)) {
                    if (!appointmentRepository.isStaffBooked(schedule.getStaffId(), start, end, null)) {
                        var staffOpt = staffReportRepository.findById(schedule.getStaffId());
                        if (staffOpt.isPresent() && "ACTIVE".equalsIgnoreCase(staffOpt.get().getStatus())) {
                            return staffOpt.get();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not query schedule candidates: {}", e.getMessage());
        }

        // 3. Fallback: Find any active unbooked staff member
        List<StaffReport> allStaff = staffReportRepository.findAll();
        for (StaffReport s : allStaff) {
            if ("ACTIVE".equalsIgnoreCase(s.getStatus()) && !appointmentRepository.isStaffBooked(s.getStaffId(), start, end, null)) {
                return s;
            }
        }

        return null;
    }

    public static AppointmentResponse mapToResponse(Appointment app, String patientName, String staffName, String roomNumber, String serialNumber) {
        return AppointmentResponse.builder()
                .id(app.getId())
                .patientId(app.getPatientId())
                .patientName(patientName)
                .staffId(app.getStaffId())
                .staffName(staffName)
                .roomId(app.getRoomId())
                .roomNumber(roomNumber)
                .machineId(app.getMachineId())
                .machineSerialNumber(serialNumber)
                .scheduledStart(app.getScheduledStart())
                .scheduledEnd(app.getScheduledEnd())
                .status(app.getStatus())
                .build();
    }
}