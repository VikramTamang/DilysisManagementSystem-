package com.fonepay.gateway.user.service.patient;

import com.fonepay.gateway.appointment.entity.Appointment;
import com.fonepay.gateway.appointment.entity.PatientOperational;
import com.fonepay.gateway.appointment.repository.AppointmentRepository;
import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPatientService {

    private final UserRepository userRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientResponse getPatientById(Long id) {
        PatientIdentity identity = patientIdentityRepository.findById(id)
                .orElseThrow(() -> new AppException("Patient identity record not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND"));

        User user = userRepository.findById(id).orElse(null);
        PatientOperational operational = patientOperationalRepository.findById(id).orElse(null);

        List<Appointment> activeAppts = appointmentRepository.findActiveAppointmentsByPatientId(identity.getId());
        String schedulingStatus = "UNSCHEDULED";
        Long activeApptId = null;
        String nextScheduledAppt = null;

        if (!activeAppts.isEmpty()) {
            Appointment first = activeAppts.get(0);
            schedulingStatus = "SCHEDULED";
            activeApptId = first.getId();
            nextScheduledAppt = first.getScheduledStart() != null ? first.getScheduledStart().toString() : null;
        }

        return CreatePatientService.mapToResponse(user, identity, operational, schedulingStatus, activeApptId, nextScheduledAppt);
    }
}
