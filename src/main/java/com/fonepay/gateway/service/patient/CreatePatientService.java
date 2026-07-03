package com.fonepay.gateway.service.patient;

import com.fonepay.gateway.dto.request.PatientRequest;
import com.fonepay.gateway.dto.response.PatientResponse;
import com.fonepay.gateway.entity.Patient;
import com.fonepay.gateway.exception.AppException;
import com.fonepay.gateway.factory.PatientFactory;
import com.fonepay.gateway.repository.PatientRepository;
import com.fonepay.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreatePatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientFactory patientFactory;

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "Email already in use",
                    HttpStatus.CONFLICT,
                    "PATIENT_EMAIL_EXISTS"
            );
        }

        Patient patient = patientFactory.toEntity(request);
        patient = patientRepository.save(patient);
        
        log.debug("Patient created with id: {}", patient.getId());
        return patientFactory.toResponse(patient);
    }
}
