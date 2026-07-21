package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.appointment.entity.PatientOperational;
import com.fonepay.gateway.appointment.repository.PatientOperationalRepository;
import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.user.entity.PatientIdentity;
import com.fonepay.gateway.user.entity.User;
import com.fonepay.gateway.user.repository.DoctorRepository;
import com.fonepay.gateway.user.repository.PatientIdentityRepository;
import com.fonepay.gateway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdatePatientService {

    private final UserRepository userRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional("userTransactionManager")
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        log.info("Updating patient ID: {}", id);

        PatientIdentity identity = patientIdentityRepository.findById(id)
                .orElseThrow(() -> new AppException("Patient identity record not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND"));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User account not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        if (request.getAssignedDoctorId() != null && !doctorRepository.existsById(request.getAssignedDoctorId())) {
            throw new AppException("Assigned doctor not found", HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND");
        }

        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        identity.setName(request.getName());
        identity.setPhone(request.getPhone());
        identity.setAddress(request.getAddress());
        identity.setDateOfBirth(request.getDateOfBirth());
        identity.setBloodGroup(request.getBloodGroup());
        PatientIdentity updatedIdentity = patientIdentityRepository.save(identity);

        PatientOperational operational = patientOperationalRepository.findById(id)
                .orElseGet(() -> PatientOperational.builder().patientId(id).totalSessions(0).build());

        operational.setAssignedDoctorId(request.getAssignedDoctorId());
        operational.setDialysisHistory(request.getDialysisHistory());
        operational.setTreatmentNotes(request.getTreatmentNotes());
        PatientOperational updatedOperational = patientOperationalRepository.save(operational);

        return CreatePatientService.mapToResponse(user, updatedIdentity, updatedOperational);
    }
}
