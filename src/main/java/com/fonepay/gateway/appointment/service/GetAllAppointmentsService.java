package com.fonepay.gateway.appointment.service;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.entity.enums.AppointmentStatus;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllAppointmentsService {

    private final AppointmentRepository appointmentRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final StaffReportRepository staffReportRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;

    public List<AppointmentResponse> getAppointments(String statusStr, Long staffId, String dateStr) {
        AppointmentStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            status = AppointmentStatus.valueOf(statusStr.toUpperCase());
        }

        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;
        if (dateStr != null && !dateStr.isBlank()) {
            LocalDate date = LocalDate.parse(dateStr);
            startOfDay = date.atStartOfDay();
            endOfDay = date.atTime(LocalTime.MAX);
        }

        return appointmentRepository.findFilteredAppointments(status, staffId, startOfDay, endOfDay)
                .stream()
                .map(this::mapAppointment)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapAppointment)
                .collect(Collectors.toList());
    }

    private AppointmentResponse mapAppointment(Appointment appointment) {
        PatientIdentity patient = patientIdentityRepository.findById(appointment.getPatientId()).orElse(null);
        StaffReport staff = staffReportRepository.findById(appointment.getStaffId()).orElse(null);
        Room room = roomRepository.findById(appointment.getRoomId()).orElse(null);
        DialysisMachine machine = dialysisMachineRepository.findById(appointment.getMachineId()).orElse(null);

        String patientName = patient != null ? patient.getName() : "Unknown Patient";
        String staffName = staff != null ? staff.getName() : "Unknown Staff";
        String roomNumber = room != null ? room.getRoomNumber() : "Unknown Room";
        String serialNumber = machine != null ? machine.getSerialNumber() : "Unknown Machine";

        return CreateAppointmentService.mapToResponse(appointment, patientName, staffName, roomNumber, serialNumber);
    }
}
