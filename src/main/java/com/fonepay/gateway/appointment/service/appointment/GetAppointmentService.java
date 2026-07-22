package com.fonepay.gateway.appointment.service.appointment;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.DialysisMachine;
import com.fonepay.gateway.appointment.entity.Room;
import com.fonepay.gateway.appointment.entity.StaffReport;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.DialysisMachineRepository;
import com.fonepay.gateway.appointment.repository.RoomRepository;
import com.fonepay.gateway.appointment.repository.StaffReportRepository;
import com.fonepay.gateway.dto.response.AppointmentResponse;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final StaffReportRepository staffReportRepository;
    private final RoomRepository roomRepository;
    private final DialysisMachineRepository dialysisMachineRepository;

    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Appointment not found", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND"));

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
