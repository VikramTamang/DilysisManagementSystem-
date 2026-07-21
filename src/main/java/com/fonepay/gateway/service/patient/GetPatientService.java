package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.appointment.entity.PatientOperational;
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

@Service
@RequiredArgsConstructor
public class GetPatientService {

    private final UserRepository userRepository;
    private final PatientIdentityRepository patientIdentityRepository;
    private final PatientOperationalRepository patientOperationalRepository;

    public PatientResponse getPatientById(Long id) {
        PatientIdentity identity = patientIdentityRepository.findById(id)
                .orElseThrow(() -> new AppException("Patient identity record not found", HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND"));

        User user = userRepository.findById(id).orElse(null);
        PatientOperational operational = patientOperationalRepository.findById(id).orElse(null);

        return CreatePatientService.mapToResponse(user, identity, operational);
    }
}
