package com.fonepay.gateway.appointment.service.appointment;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.service.report.AppointmentAuditLogService;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
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
    private final StaffReportRepository staffReportRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;
    private final AppointmentAuditLogService appointmentAuditLogService;

    @Transactional("appointmentTransactionManager")
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        return createAppointment(request, null, null);
    }

    @Transactional("appointmentTransactionManager")
    public AppointmentResponse createAppointment(AppointmentRequest request, Long performedByUserId, String performedByRole) {
        log.info("Creating appointment for patientId: {}, staffId: {}", request.getPatientId(), request.getStaffId());

        if (request.getScheduledStart().isAfter(request.getScheduledEnd()) || request.getScheduledStart().isEqual(request.getScheduledEnd())) {
            throw new AppException("Scheduled start time must be before end time", HttpStatus.BAD_REQUEST, "INVALID_SCHEDULE_TIME");
        }

        PatientIdentity patient = patientIdentityRepository.findById(request.getPatientId())
                .orElseThrow(() -> new AppException("Patient not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND"));

        StaffReport staff = staffReportRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException("Staff member not found", HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND"));

        // Overlap Check for Staff
        if (appointmentRepository.isStaffBooked(staff.getStaffId(), request.getScheduledStart(), request.getScheduledEnd(), null)) {
            throw new AppException("Staff is already booked during this time", HttpStatus.BAD_REQUEST, "STAFF_ALREADY_BOOKED");
        }

        // Resolve Room
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

        // Resolve Machine
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

        Appointment appointment = Appointment.builder()
                .patientId(patient.getId())
                .staffId(staff.getStaffId())
                .roomId(room.getId())
                .machineId(machine.getId())
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        appointmentAuditLogService.logCreated(saved, performedByUserId, performedByRole);

        return mapToResponse(saved, patient.getName(), staff.getName(), room.getRoomNumber(), machine.getSerialNumber());
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